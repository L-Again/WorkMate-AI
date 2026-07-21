package com.workmate.ai.service.impl;

import com.workmate.ai.client.LlmClient;
import com.workmate.ai.client.LlmRequest;
import com.workmate.ai.client.LlmResponse;
import com.workmate.ai.common.ErrorCode;
import com.workmate.ai.dto.AgentChatDTO;
import com.workmate.ai.entity.ChatMessage;
import com.workmate.ai.entity.ChatSession;
import com.workmate.ai.entity.KnowledgeReference;
import com.workmate.ai.entity.SysUser;
import com.workmate.ai.exception.BusinessException;
import com.workmate.ai.mapper.ChatMessageMapper;
import com.workmate.ai.mapper.ChatSessionMapper;
import com.workmate.ai.mapper.KnowledgeReferenceMapper;
import com.workmate.ai.mapper.SysUserMapper;
import com.workmate.ai.service.AgentAnswerCacheService;
import com.workmate.ai.service.AgentService;
import com.workmate.ai.service.KnowledgeService;
import com.workmate.ai.service.PromptBuilder;
import com.workmate.ai.vo.AgentAnswerCacheValue;
import com.workmate.ai.vo.AgentAnswerVO;
import com.workmate.ai.vo.AgentTraceStepVO;
import com.workmate.ai.vo.KnowledgeDetailVO;
import com.workmate.ai.vo.KnowledgeListItemVO;
import com.workmate.ai.vo.KnowledgeReferenceVO;
import com.workmate.ai.vo.PromptKnowledgeItem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.workmate.ai.dto.ModelCallLogCreateDTO;
import com.workmate.ai.service.ModelCallLogService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AgentServiceImpl implements AgentService {

    private static final int ENABLED_STATUS = 1;
    private static final int NOT_DELETED = 0;
    private static final int NOT_FROM_CACHE = 0;
    private static final int FROM_CACHE = 1;
    private static final int CANNOT_CREATE_TICKET = 0;
    private static final int CAN_CREATE_TICKET = 1;
    private static final String USER_ROLE = "USER";
    private static final String ASSISTANT_ROLE = "ASSISTANT";
    private static final String DEFAULT_MODEL_NAME = "mock-llm";
    private static final String INSUFFICIENT_KNOWLEDGE_ANSWER = "当前知识库中没有找到可靠内容。你可以创建人工咨询工单。";
    private static final String LOG_STATUS_CACHE_HIT = "CACHE_HIT";
    private static final String LOG_STATUS_SUCCESS = "SUCCESS";
    private static final String LOG_STATUS_FAILED = "FAILED";
    private static final String LOG_STATUS_NO_KNOWLEDGE = "NO_KNOWLEDGE";

    private final SysUserMapper sysUserMapper;
    private final ChatSessionMapper chatSessionMapper;
    private final ChatMessageMapper chatMessageMapper;
    private final KnowledgeReferenceMapper knowledgeReferenceMapper;
    private final KnowledgeService knowledgeService;
    private final PromptBuilder promptBuilder;
    private final LlmClient llmClient;
    private final AgentAnswerCacheService agentAnswerCacheService;
    private final ModelCallLogService modelCallLogService;
    private final String llmModelName;

    public AgentServiceImpl(SysUserMapper sysUserMapper,
                            ChatSessionMapper chatSessionMapper,
                            ChatMessageMapper chatMessageMapper,
                            KnowledgeReferenceMapper knowledgeReferenceMapper,
                            KnowledgeService knowledgeService,
                            PromptBuilder promptBuilder,
                            LlmClient llmClient,
                            AgentAnswerCacheService agentAnswerCacheService,
                            ModelCallLogService modelCallLogService,
                            @Value("${workmate.llm.model:mock-llm}") String llmModelName) {
        this.sysUserMapper = sysUserMapper;
        this.chatSessionMapper = chatSessionMapper;
        this.chatMessageMapper = chatMessageMapper;
        this.knowledgeReferenceMapper = knowledgeReferenceMapper;
        this.knowledgeService = knowledgeService;
        this.promptBuilder = promptBuilder;
        this.llmClient = llmClient;
        this.agentAnswerCacheService = agentAnswerCacheService;
        this.modelCallLogService = modelCallLogService;
        this.llmModelName = normalizeModelName(llmModelName);
    }

    @Override
    public AgentAnswerVO chat(Long userId, AgentChatDTO request) {
        validateEnabledUser(userId);
        getOwnedActiveSession(userId, request.getSessionId());

        List<AgentTraceStepVO> traceSteps = new ArrayList<>();
        long startTimeMillis = System.currentTimeMillis();

        String question = normalizeQuestion(request.getQuestion());
        ChatMessage questionMessage = saveMessage(
                request.getSessionId(),
                userId,
                USER_ROLE,
                question,
                NOT_FROM_CACHE,
                CANNOT_CREATE_TICKET
        );
        traceSteps.add(new AgentTraceStepVO(
                "MESSAGE_SAVE",
                "保存用户消息",
                true,
                "用户消息已保存"
        ));

        Optional<AgentAnswerCacheValue> cachedAnswer = getCachedAnswer(question);
        if (cachedAnswer.isPresent()) {
            traceSteps.add(new AgentTraceStepVO(
                    "CACHE_LOOKUP",
                    "检查 Redis 问答缓存",
                    true,
                    "缓存命中"
            ));

            AgentAnswerCacheValue cacheValue = cachedAnswer.get();
            ChatMessage answerMessage = saveMessage(
                    request.getSessionId(),
                    userId,
                    ASSISTANT_ROLE,
                    cacheValue.getAnswer(),
                    FROM_CACHE,
                    CANNOT_CREATE_TICKET
            );
            saveKnowledgeReferencesFromCache(answerMessage.getId(), cacheValue.getReferences());
            traceSteps.add(new AgentTraceStepVO(
                    "MESSAGE_SAVE",
                    "保存缓存助手回答",
                    true,
                    "缓存助手消息已保存"
            ));

            recordModelCallLog(
                    userId,
                    request.getSessionId(),
                    questionMessage.getId(),
                    answerMessage.getId(),
                    null,
                    true,
                    LOG_STATUS_CACHE_HIT,
                    startTimeMillis,
                    null,
                    null,
                    null
            );

            return new AgentAnswerVO(
                    request.getSessionId(),
                    questionMessage.getId(),
                    answerMessage.getId(),
                    cacheValue.getAnswer(),
                    true,
                    false,
                    cacheValue.getReferences(),
                    traceSteps
            );
        }

        traceSteps.add(new AgentTraceStepVO(
                "CACHE_LOOKUP",
                "检查 Redis 问答缓存",
                true,
                "缓存未命中"
        ));

        List<KnowledgeListItemVO> knowledgeList = knowledgeService.searchKnowledge(userId, question, 5);
        traceSteps.add(new AgentTraceStepVO(
                "KNOWLEDGE_SEARCH",
                "检索企业知识库",
                true,
                "找到 " + knowledgeList.size() + " 条相关知识"
        ));

        if (knowledgeList.isEmpty()) {
            ChatMessage insufficientKnowledgeMessage = saveMessage(
                    request.getSessionId(),
                    userId,
                    ASSISTANT_ROLE,
                    INSUFFICIENT_KNOWLEDGE_ANSWER,
                    NOT_FROM_CACHE,
                    CAN_CREATE_TICKET
            );
            traceSteps.add(new AgentTraceStepVO(
                    "MESSAGE_SAVE",
                    "保存知识不足提示",
                    true,
                    "知识不足提示消息已保存"
            ));

            recordModelCallLog(
                    userId,
                    request.getSessionId(),
                    questionMessage.getId(),
                    insufficientKnowledgeMessage.getId(),
                    null,
                    false,
                    LOG_STATUS_NO_KNOWLEDGE,
                    startTimeMillis,
                    null,
                    null,
                    null
            );

            return new AgentAnswerVO(
                    request.getSessionId(),
                    questionMessage.getId(),
                    insufficientKnowledgeMessage.getId(),
                    INSUFFICIENT_KNOWLEDGE_ANSWER,
                    false,
                    true,
                    List.of(),
                    traceSteps
            );
        }

        List<PromptKnowledgeItem> promptKnowledgeItems = buildPromptKnowledgeItems(userId, knowledgeList);
        String prompt = promptBuilder.buildKnowledgeAnswerPrompt(question, promptKnowledgeItems);

        LlmResponse llmResponse;
        try {
            llmResponse = llmClient.chat(new LlmRequest(llmModelName, prompt));
        } catch (RuntimeException exception) {
            traceSteps.add(new AgentTraceStepVO(
                    "LLM_CALL",
                    "调用大模型生成回答",
                    false,
                    "调用失败"
            ));
            recordModelCallLog(
                    userId,
                    request.getSessionId(),
                    questionMessage.getId(),
                    null,
                    llmModelName,
                    false,
                    LOG_STATUS_FAILED,
                    startTimeMillis,
                    null,
                    null,
                    exception.getMessage()
            );
            throw new BusinessException(ErrorCode.LLM_CALL_FAILED);
        }

        traceSteps.add(new AgentTraceStepVO(
                "LLM_CALL",
                "调用大模型生成回答",
                true,
                "调用成功"
        ));

        ChatMessage answerMessage = saveMessage(
                request.getSessionId(),
                userId,
                ASSISTANT_ROLE,
                llmResponse.getAnswer(),
                NOT_FROM_CACHE,
                CANNOT_CREATE_TICKET
        );
        List<KnowledgeReferenceVO> references = buildReferences(knowledgeList);
        saveKnowledgeReferences(answerMessage.getId(), knowledgeList);
        saveAnswerCache(question, new AgentAnswerCacheValue(llmResponse.getAnswer(), references));

        traceSteps.add(new AgentTraceStepVO(
                "MESSAGE_SAVE",
                "保存助手回答",
                true,
                "助手消息已保存"
        ));

        recordModelCallLog(
                userId,
                request.getSessionId(),
                questionMessage.getId(),
                answerMessage.getId(),
                llmResponse.getModelName(),
                false,
                LOG_STATUS_SUCCESS,
                startTimeMillis,
                llmResponse.getPromptTokens(),
                llmResponse.getCompletionTokens(),
                null
        );

        return new AgentAnswerVO(
                request.getSessionId(),
                questionMessage.getId(),
                answerMessage.getId(),
                llmResponse.getAnswer(),
                false,
                false,
                references,
                traceSteps
        );
    }

    private void validateEnabledUser(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null || !Integer.valueOf(ENABLED_STATUS).equals(user.getStatus())) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND_OR_DISABLED);
        }
    }

    private ChatSession getOwnedActiveSession(Long userId, Long sessionId) {
        ChatSession session = chatSessionMapper.selectById(sessionId);
        if (session == null
                || !Integer.valueOf(NOT_DELETED).equals(session.getIsDeleted())
                || !userId.equals(session.getUserId())) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND);
        }
        return session;
    }

    private ChatMessage saveMessage(Long sessionId,
                                    Long userId,
                                    String role,
                                    String content,
                                    Integer fromCache,
                                    Integer canCreateTicket) {
        ChatMessage message = new ChatMessage();
        message.setSessionId(sessionId);
        message.setUserId(userId);
        message.setRole(role);
        message.setContent(content);
        message.setFromCache(fromCache);
        message.setCanCreateTicket(canCreateTicket);
        message.setIsDeleted(NOT_DELETED);

        chatMessageMapper.insert(message);
        updateSessionLastMessageAt(sessionId);

        return message;
    }

    private void updateSessionLastMessageAt(Long sessionId) {
        ChatSession session = new ChatSession();
        session.setId(sessionId);
        session.setLastMessageAt(LocalDateTime.now());

        chatSessionMapper.updateById(session);
    }

    private List<PromptKnowledgeItem> buildPromptKnowledgeItems(Long userId, List<KnowledgeListItemVO> knowledgeList) {
        List<PromptKnowledgeItem> items = new ArrayList<>();
        for (KnowledgeListItemVO knowledge : knowledgeList) {
            KnowledgeDetailVO detail = knowledgeService.getKnowledgeDetail(userId, knowledge.getId());
            items.add(new PromptKnowledgeItem(
                    detail.getId(),
                    detail.getTitle(),
                    detail.getCategoryName(),
                    detail.getContent()
            ));
        }
        return items;
    }

    private List<KnowledgeReferenceVO> buildReferences(List<KnowledgeListItemVO> knowledgeList) {
        List<KnowledgeReferenceVO> references = new ArrayList<>();
        for (KnowledgeListItemVO knowledge : knowledgeList) {
            references.add(new KnowledgeReferenceVO(
                    knowledge.getId(),
                    knowledge.getTitle(),
                    knowledge.getCategoryName()
            ));
        }
        return references;
    }

    private Optional<AgentAnswerCacheValue> getCachedAnswer(String question) {
        try {
            return agentAnswerCacheService.get(buildModelScopedCacheQuestion(question));
        } catch (RuntimeException exception) {
            return Optional.empty();
        }
    }

    private void saveAnswerCache(String question, AgentAnswerCacheValue cacheValue) {
        try {
            agentAnswerCacheService.save(buildModelScopedCacheQuestion(question), cacheValue);
        } catch (RuntimeException exception) {
            // Redis cache failure must not block the Agent answer.
        }
    }

    private String buildModelScopedCacheQuestion(String question) {
        return "model=" + llmModelName + "\nquestion=" + question;
    }

    private void saveKnowledgeReferences(Long answerMessageId, List<KnowledgeListItemVO> knowledgeList) {
        for (KnowledgeListItemVO knowledge : knowledgeList) {
            saveKnowledgeReference(answerMessageId, knowledge.getId());
        }
    }

    private void saveKnowledgeReferencesFromCache(Long answerMessageId, List<KnowledgeReferenceVO> references) {
        for (KnowledgeReferenceVO reference : references) {
            saveKnowledgeReference(answerMessageId, reference.getKnowledgeId());
        }
    }

    private void saveKnowledgeReference(Long answerMessageId, Long knowledgeId) {
        KnowledgeReference reference = new KnowledgeReference();
        reference.setMessageId(answerMessageId);
        reference.setKnowledgeId(knowledgeId);

        knowledgeReferenceMapper.insert(reference);
    }

    private void recordModelCallLog(Long userId,
                                    Long sessionId,
                                    Long questionMessageId,
                                    Long answerMessageId,
                                    String modelName,
                                    Boolean fromCache,
                                    String callStatus,
                                    long startTimeMillis,
                                    Integer promptTokens,
                                    Integer completionTokens,
                                    String errorMessage) {
        ModelCallLogCreateDTO logRequest = new ModelCallLogCreateDTO();
        logRequest.setUserId(userId);
        logRequest.setSessionId(sessionId);
        logRequest.setQuestionMessageId(questionMessageId);
        logRequest.setAnswerMessageId(answerMessageId);
        logRequest.setModelName(modelName);
        logRequest.setFromCache(fromCache);
        logRequest.setCallStatus(callStatus);
        logRequest.setDurationMs(System.currentTimeMillis() - startTimeMillis);
        logRequest.setPromptTokens(promptTokens);
        logRequest.setCompletionTokens(completionTokens);
        logRequest.setErrorMessage(errorMessage);

        modelCallLogService.recordAsync(logRequest);
    }

    private String normalizeQuestion(String question) {
        if (question == null) {
            return "";
        }
        return question.trim().replaceAll("\\s+", " ");
    }

    private String normalizeModelName(String modelName) {
        if (modelName == null || modelName.trim().isEmpty()) {
            return DEFAULT_MODEL_NAME;
        }
        return modelName.trim();
    }
}
