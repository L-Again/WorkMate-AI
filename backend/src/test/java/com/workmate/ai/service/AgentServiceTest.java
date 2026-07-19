package com.workmate.ai.service;

import com.workmate.ai.client.LlmClient;
import com.workmate.ai.client.LlmRequest;
import com.workmate.ai.client.LlmResponse;
import com.workmate.ai.dto.AgentChatDTO;
import com.workmate.ai.entity.ChatMessage;
import com.workmate.ai.entity.ChatSession;
import com.workmate.ai.entity.SysUser;
import com.workmate.ai.mapper.ChatMessageMapper;
import com.workmate.ai.mapper.ChatSessionMapper;
import com.workmate.ai.mapper.SysUserMapper;
import com.workmate.ai.service.impl.AgentServiceImpl;
import com.workmate.ai.vo.AgentAnswerVO;
import com.workmate.ai.vo.KnowledgeDetailVO;
import com.workmate.ai.vo.KnowledgeListItemVO;
import com.workmate.ai.vo.PromptKnowledgeItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgentServiceTest {

    @Mock
    private SysUserMapper sysUserMapper;

    @Mock
    private ChatSessionMapper chatSessionMapper;

    @Mock
    private ChatMessageMapper chatMessageMapper;

    @Mock
    private KnowledgeService knowledgeService;

    @Mock
    private PromptBuilder promptBuilder;

    @Mock
    private LlmClient llmClient;

    private AgentService agentService;

    @BeforeEach
    void setUp() {
        agentService = new AgentServiceImpl(
                sysUserMapper,
                chatSessionMapper,
                chatMessageMapper,
                knowledgeService,
                promptBuilder,
                llmClient
        );
    }

    @Test
    void shouldChatWithAgentHappyPath() {
        AgentChatDTO request = new AgentChatDTO();
        request.setSessionId(10L);
        request.setQuestion("  Git   分支应该怎么命名？ ");

        when(sysUserMapper.selectById(1L)).thenReturn(user(1L, 1));
        when(chatSessionMapper.selectById(10L)).thenReturn(session(10L, 1L, 0));

        when(chatMessageMapper.insert(any(ChatMessage.class))).thenAnswer(invocation -> {
            ChatMessage message = invocation.getArgument(0);
            if ("USER".equals(message.getRole())) {
                message.setId(101L);
            } else {
                message.setId(102L);
            }
            return 1;
        });

        when(knowledgeService.searchKnowledge(1L, "Git 分支应该怎么命名？", 5))
                .thenReturn(List.of(new KnowledgeListItemVO(
                        3L,
                        3L,
                        "研发规范",
                        "Git 分支命名规范",
                        "Git,分支,branch",
                        1,
                        LocalDateTime.of(2026, 7, 19, 13, 50)
                )));

        when(knowledgeService.getKnowledgeDetail(1L, 3L))
                .thenReturn(new KnowledgeDetailVO(
                        3L,
                        3L,
                        "研发规范",
                        "Git 分支命名规范",
                        "Git,分支,branch",
                        "功能分支统一使用 feature/功能名称。",
                        1,
                        LocalDateTime.of(2026, 7, 19, 13, 50)
                ));

        when(promptBuilder.buildKnowledgeAnswerPrompt(eq("Git 分支应该怎么命名？"), any()))
                .thenReturn("测试 Prompt");

        when(llmClient.chat(any(LlmRequest.class)))
                .thenReturn(new LlmResponse(
                        "这是 Mock LLM 根据企业知识生成的测试回答。",
                        "mock-llm",
                        null,
                        null
                ));

        AgentAnswerVO result = agentService.chat(1L, request);

        assertThat(result.getSessionId()).isEqualTo(10L);
        assertThat(result.getQuestionMessageId()).isEqualTo(101L);
        assertThat(result.getAnswerMessageId()).isEqualTo(102L);
        assertThat(result.getAnswer()).isEqualTo("这是 Mock LLM 根据企业知识生成的测试回答。");
        assertThat(result.getFromCache()).isFalse();
        assertThat(result.getCanCreateTicket()).isFalse();
        assertThat(result.getReferences()).hasSize(1);
        assertThat(result.getReferences().get(0).getKnowledgeId()).isEqualTo(3L);
        assertThat(result.getTraceSteps()).hasSize(4);

        ArgumentCaptor<ChatMessage> messageCaptor = ArgumentCaptor.forClass(ChatMessage.class);
        verify(chatMessageMapper, times(2)).insert(messageCaptor.capture());

        List<ChatMessage> savedMessages = messageCaptor.getAllValues();
        assertThat(savedMessages.get(0).getRole()).isEqualTo("USER");
        assertThat(savedMessages.get(0).getContent()).isEqualTo("Git 分支应该怎么命名？");
        assertThat(savedMessages.get(1).getRole()).isEqualTo("ASSISTANT");
        assertThat(savedMessages.get(1).getContent()).isEqualTo("这是 Mock LLM 根据企业知识生成的测试回答。");

        ArgumentCaptor<List<PromptKnowledgeItem>> promptItemsCaptor = ArgumentCaptor.forClass(List.class);
        verify(promptBuilder).buildKnowledgeAnswerPrompt(eq("Git 分支应该怎么命名？"), promptItemsCaptor.capture());

        List<PromptKnowledgeItem> promptItems = promptItemsCaptor.getValue();
        assertThat(promptItems).hasSize(1);
        assertThat(promptItems.get(0).getContent()).isEqualTo("功能分支统一使用 feature/功能名称。");

        ArgumentCaptor<LlmRequest> llmRequestCaptor = ArgumentCaptor.forClass(LlmRequest.class);
        verify(llmClient).chat(llmRequestCaptor.capture());
        assertThat(llmRequestCaptor.getValue().getModelName()).isEqualTo("mock-llm");
        assertThat(llmRequestCaptor.getValue().getPrompt()).isEqualTo("测试 Prompt");

        verify(chatSessionMapper, times(2)).updateById(any(ChatSession.class));
    }

    private SysUser user(Long id, Integer status) {
        SysUser user = new SysUser();
        user.setId(id);
        user.setStatus(status);
        return user;
    }

    private ChatSession session(Long id, Long userId, Integer isDeleted) {
        ChatSession session = new ChatSession();
        session.setId(id);
        session.setUserId(userId);
        session.setIsDeleted(isDeleted);
        return session;
    }
}