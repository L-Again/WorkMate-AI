package com.workmate.ai.controller;

import com.workmate.ai.dto.AgentChatDTO;
import com.workmate.ai.exception.GlobalExceptionHandler;
import com.workmate.ai.service.AgentService;
import com.workmate.ai.vo.AgentAnswerVO;
import com.workmate.ai.vo.AgentTraceStepVO;
import com.workmate.ai.vo.KnowledgeReferenceVO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AgentController.class)
@Import(GlobalExceptionHandler.class)
class AgentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AgentService agentService;

    @Test
    void shouldChatWithAgent() throws Exception {
        when(agentService.chat(eq(1L), any(AgentChatDTO.class)))
                .thenReturn(new AgentAnswerVO(
                        10L,
                        101L,
                        102L,
                        "这是 Mock LLM 根据企业知识生成的测试回答。",
                        false,
                        false,
                        List.of(new KnowledgeReferenceVO(
                                3L,
                                "Git 分支命名规范",
                                "研发规范"
                        )),
                        List.of(new AgentTraceStepVO(
                                "LLM_CALL",
                                "调用大模型生成回答",
                                true,
                                "调用成功"
                        ))
                ));

        mockMvc.perform(post("/api/agent/chat")
                        .header("X-User-Id", "1")
                        .contentType("application/json")
                        .content("""
                            {
                              "sessionId": 10,
                              "question": "Git 分支应该怎么命名？"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(200)))
                .andExpect(jsonPath("$.data.sessionId", is(10)))
                .andExpect(jsonPath("$.data.questionMessageId", is(101)))
                .andExpect(jsonPath("$.data.answerMessageId", is(102)))
                .andExpect(jsonPath("$.data.answer", is("这是 Mock LLM 根据企业知识生成的测试回答。")))
                .andExpect(jsonPath("$.data.fromCache", is(false)))
                .andExpect(jsonPath("$.data.canCreateTicket", is(false)))
                .andExpect(jsonPath("$.data.references[0].knowledgeId", is(3)))
                .andExpect(jsonPath("$.data.references[0].title", is("Git 分支命名规范")))
                .andExpect(jsonPath("$.data.traceSteps[0].step", is("LLM_CALL")));
    }
}