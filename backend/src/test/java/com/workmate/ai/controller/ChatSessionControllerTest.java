package com.workmate.ai.controller;

import com.workmate.ai.dto.SessionCreateDTO;
import com.workmate.ai.dto.SessionTitleUpdateDTO;
import com.workmate.ai.exception.GlobalExceptionHandler;
import com.workmate.ai.service.ChatSessionService;
import com.workmate.ai.vo.SessionVO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChatSessionController.class)
@Import(GlobalExceptionHandler.class)
class ChatSessionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChatSessionService chatSessionService;

    @Test
    void shouldCreateSession() throws Exception {
        when(chatSessionService.createSession(eq(1L), any(SessionCreateDTO.class)))
                .thenReturn(new SessionVO(
                        10L,
                        "Git 规范咨询",
                        null,
                        LocalDateTime.of(2026, 7, 19, 11, 40)
                ));

        mockMvc.perform(post("/api/chat/sessions")
                        .header("X-User-Id", "1")
                        .contentType("application/json")
                        .content("""
                            {
                              "title": "Git 规范咨询"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(200)))
                .andExpect(jsonPath("$.data.sessionId", is(10)))
                .andExpect(jsonPath("$.data.title", is("Git 规范咨询")));
    }

    @Test
    void shouldGetSessionDetail() throws Exception {
        when(chatSessionService.getSessionDetail(1L, 10L))
                .thenReturn(new SessionVO(
                        10L,
                        "Git 规范咨询",
                        null,
                        LocalDateTime.of(2026, 7, 19, 11, 40)
                ));

        mockMvc.perform(get("/api/chat/sessions/{sessionId}", 10L)
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(200)))
                .andExpect(jsonPath("$.data.sessionId", is(10)))
                .andExpect(jsonPath("$.data.title", is("Git 规范咨询")));
    }

    @Test
    void shouldUpdateSessionTitle() throws Exception {
        when(chatSessionService.updateSessionTitle(eq(1L), eq(10L), any(SessionTitleUpdateDTO.class)))
                .thenReturn(new SessionVO(
                        10L,
                        "Git 与代码提交规范",
                        null,
                        LocalDateTime.of(2026, 7, 19, 11, 40)
                ));

        mockMvc.perform(patch("/api/chat/sessions/{sessionId}/title", 10L)
                        .header("X-User-Id", "1")
                        .contentType("application/json")
                        .content("""
                            {
                              "title": "Git 与代码提交规范"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(200)))
                .andExpect(jsonPath("$.data.sessionId", is(10)))
                .andExpect(jsonPath("$.data.title", is("Git 与代码提交规范")));
    }

    @Test
    void shouldDeleteSession() throws Exception {
        when(chatSessionService.deleteSession(1L, 10L)).thenReturn(true);

        mockMvc.perform(delete("/api/chat/sessions/{sessionId}", 10L)
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(200)))
                .andExpect(jsonPath("$.data", is(true)));
    }
}