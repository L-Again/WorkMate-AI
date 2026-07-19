package com.workmate.ai.service.impl;

import com.workmate.ai.client.LlmClient;
import com.workmate.ai.client.LlmRequest;
import com.workmate.ai.client.LlmResponse;
import com.workmate.ai.common.ErrorCode;
import com.workmate.ai.dto.AgentChatDTO;
import com.workmate.ai.entity.ChatMessage;
import com.workmate.ai.entity.ChatSession;
import com.workmate.ai.entity.SysUser;
import com.workmate.ai.exception.BusinessException;
import com.workmate.ai.mapper.ChatMessageMapper;
import com.workmate.ai.mapper.ChatSessionMapper;
import com.workmate.ai.mapper.SysUserMapper;
import com.workmate.ai.service.AgentService;
import com.workmate.ai.service.KnowledgeService;
import com.workmate.ai.service.PromptBuilder;
import com.workmate.ai.vo.AgentAnswerVO;
import com.workmate.ai.vo.AgentTraceStepVO;
import com.workmate.ai.vo.KnowledgeDetailVO;
import com.workmate.ai.vo.KnowledgeListItemVO;
import com.workmate.ai.vo.KnowledgeReferenceVO;
import com.workmate.ai.vo.PromptKnowledgeItem;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AgentServiceImpl implements AgentService {

    private static final int ENABLED_STATUS = 1;
    private static final int NOT_DELETED = 0;
    private static final int NOT_FROM_CACHE = 0;
    private static final int CANNOT_CREATE_TICKET = 0;
    private static final String USER_ROLE = "USER";
    private static final String ASSISTANT_ROLE = "ASSISTANT";
    private static final String MOCK_MODEL_NAME = "mock-llm";

    private final SysUserMapper sysUserMapper;
    private final ChatSessionMapper chatSessionMapper;
    private final ChatMessageMapper chatMessageMapper;
    private final KnowledgeService knowledgeService;
    private final PromptBuilder promptBuilder;
    private final LlmClient llmClient;

    public AgentServiceImpl(SysUserMapper sysUserMapper,
                            ChatSessionMapper chatSessionMapper,
                            ChatMessageMapper chatMessageMapper,
                            KnowledgeService knowledgeService,
                            PromptBuilder promptBuilder,
                            LlmClient llmClient) {
        this.sysUserMapper = sysUserMapper;
        this.chatSessionMapper = chatSessionMapper;
        this.chatMessageMapper = chatMessageMapper;
        this.knowledgeService = knowledgeService;
        this.promptBuilder = promptBuilder;
        this.llmClient = llmClient;
    }

    @Override
    public AgentAnswerVO chat(Long userId, AgentChatDTO request) {
        validateEnabledUser(userId);
        getOwnedActiveSession(userId, request.getSessionId());

        List<AgentTraceStepVO> traceSteps = new ArrayList<>();

        String question = normalizeQuestion(request.getQuestion());
        ChatMessage questionMessage = saveMessage(request.getSessionId(), userId, USER_ROLE, question);
        traceSteps.add(new AgentTraceStepVO(
                "MESSAGE_SAVE",
                "保存用户消息",
                true,
                "用户消息已保存"
        ));

        List<KnowledgeListItemVO> knowledgeList = knowledgeService.searchKnowledge(userId, question, 5);
        traceSteps.add(new AgentTraceStepVO(
                "KNOWLEDGE_SEARCH",
                "检索企业知识库",
                true,
                "找到 " + knowledgeList.size() + " 条相关知识"
        ));

        List<PromptKnowledgeItem> promptKnowledgeItems = buildPromptKnowledgeItems(userId, knowledgeList);
        String prompt = promptBuilder.buildKnowledgeAnswerPrompt(question, promptKnowledgeItems);

        LlmResponse llmResponse = llmClient.chat(new LlmRequest(MOCK_MODEL_NAME, prompt));
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
                llmResponse.getAnswer()
        );
        traceSteps.add(new AgentTraceStepVO(
                "MESSAGE_SAVE",
                "保存助手回答",
                true,
                "助手消息已保存"
        ));

        return new AgentAnswerVO(
                request.getSessionId(),
                questionMessage.getId(),
                answerMessage.getId(),
                llmResponse.getAnswer(),
                false,
                false,
                buildReferences(knowledgeList),
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

    private ChatMessage saveMessage(Long sessionId, Long userId, String role, String content) {
        ChatMessage message = new ChatMessage();
        message.setSessionId(sessionId);
        message.setUserId(userId);
        message.setRole(role);
        message.setContent(content);
        message.setFromCache(NOT_FROM_CACHE);
        message.setCanCreateTicket(CANNOT_CREATE_TICKET);
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

    private String normalizeQuestion(String question) {
        if (question == null) {
            return "";
        }
        return question.trim().replaceAll("\\s+", " ");
    }
}