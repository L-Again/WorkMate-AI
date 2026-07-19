package com.workmate.ai.controller;

import com.workmate.ai.common.PageResult;
import com.workmate.ai.dto.TicketCreateDTO;
import com.workmate.ai.dto.TicketStatusUpdateDTO;
import com.workmate.ai.exception.BusinessException;
import com.workmate.ai.exception.GlobalExceptionHandler;
import com.workmate.ai.common.ErrorCode;
import com.workmate.ai.service.TicketService;
import com.workmate.ai.vo.TicketListVO;
import com.workmate.ai.vo.TicketVO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TicketController.class)
@Import(GlobalExceptionHandler.class)
class TicketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TicketService ticketService;

    @Test
    void shouldCreateTicket() throws Exception {
        when(ticketService.createTicket(eq(1L), any(TicketCreateDTO.class)))
                .thenReturn(ticketVO("PENDING"));

        mockMvc.perform(post("/api/tickets")
                        .header("X-User-Id", "1")
                        .contentType("application/json")
                        .content("""
                            {
                              "sessionId": 9,
                              "questionMessageId": 11,
                              "title": "咨询火星基地氧气补贴流程",
                              "description": "Agent 未找到相关知识，希望人工确认。"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(200)))
                .andExpect(jsonPath("$.data.ticketId", is(1)))
                .andExpect(jsonPath("$.data.ticketNo", is("TK20260719182717796271518")))
                .andExpect(jsonPath("$.data.status", is("PENDING")));
    }

    @Test
    void shouldListMyTickets() throws Exception {
        when(ticketService.listMyTickets(1L, 1L, 10L, "PENDING"))
                .thenReturn(new PageResult<>(
                        List.of(ticketListVO("PENDING")),
                        1L,
                        10L,
                        1L,
                        1L
                ));

        mockMvc.perform(get("/api/tickets/my")
                        .header("X-User-Id", "1")
                        .param("pageNum", "1")
                        .param("pageSize", "10")
                        .param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(200)))
                .andExpect(jsonPath("$.data.records", hasSize(1)))
                .andExpect(jsonPath("$.data.records[0].ticketId", is(1)))
                .andExpect(jsonPath("$.data.records[0].status", is("PENDING")))
                .andExpect(jsonPath("$.data.total", is(1)));
    }

    @Test
    void shouldGetTicketDetail() throws Exception {
        when(ticketService.getTicketDetail(1L, 1L)).thenReturn(ticketVO("PENDING"));

        mockMvc.perform(get("/api/tickets/{ticketId}", 1L)
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(200)))
                .andExpect(jsonPath("$.data.ticketId", is(1)))
                .andExpect(jsonPath("$.data.sessionId", is(9)))
                .andExpect(jsonPath("$.data.questionMessageId", is(11)));
    }

    @Test
    void shouldListAdminTickets() throws Exception {
        when(ticketService.listAdminTickets(2L, 1L, 10L, null, null))
                .thenReturn(new PageResult<>(
                        List.of(ticketListVO("PENDING")),
                        1L,
                        10L,
                        1L,
                        1L
                ));

        mockMvc.perform(get("/api/admin/tickets")
                        .header("X-User-Id", "2")
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(200)))
                .andExpect(jsonPath("$.data.records", hasSize(1)))
                .andExpect(jsonPath("$.data.records[0].ticketId", is(1)));
    }

    @Test
    void shouldUpdateTicketStatus() throws Exception {
        when(ticketService.updateTicketStatus(eq(2L), eq(1L), any(TicketStatusUpdateDTO.class)))
                .thenReturn(resolvedTicketVO());

        mockMvc.perform(patch("/api/admin/tickets/{ticketId}/status", 1L)
                        .header("X-User-Id", "2")
                        .contentType("application/json")
                        .content("""
                            {
                              "status": "RESOLVED",
                              "resolution": "由项目负责人提交资产申请，审批后由 IT 配置。"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(200)))
                .andExpect(jsonPath("$.data.ticketId", is(1)))
                .andExpect(jsonPath("$.data.status", is("RESOLVED")))
                .andExpect(jsonPath("$.data.handledBy", is(2)))
                .andExpect(jsonPath("$.data.resolution", is("由项目负责人提交资产申请，审批后由 IT 配置。")));
    }

    @Test
    void shouldReturnForbiddenWhenEmployeeListsAdminTickets() throws Exception {
        when(ticketService.listAdminTickets(1L, 1L, 10L, null, null))
                .thenThrow(new BusinessException(ErrorCode.FORBIDDEN));

        mockMvc.perform(get("/api/admin/tickets")
                        .header("X-User-Id", "1")
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(40301)))
                .andExpect(jsonPath("$.message", is("无权限")));
    }

    private TicketVO ticketVO(String status) {
        return new TicketVO(
                1L,
                "TK20260719182717796271518",
                1L,
                9L,
                11L,
                "咨询火星基地氧气补贴流程",
                "Agent 未找到相关知识，希望人工确认。",
                status,
                null,
                null,
                null,
                LocalDateTime.of(2026, 7, 19, 18, 27, 17),
                LocalDateTime.of(2026, 7, 19, 18, 27, 17)
        );
    }

    private TicketVO resolvedTicketVO() {
        return new TicketVO(
                1L,
                "TK20260719182717796271518",
                1L,
                9L,
                11L,
                "咨询火星基地氧气补贴流程",
                "Agent 未找到相关知识，希望人工确认。",
                "RESOLVED",
                "由项目负责人提交资产申请，审批后由 IT 配置。",
                2L,
                LocalDateTime.of(2026, 7, 19, 18, 31, 49),
                LocalDateTime.of(2026, 7, 19, 18, 27, 17),
                LocalDateTime.of(2026, 7, 19, 18, 31, 48)
        );
    }

    private TicketListVO ticketListVO(String status) {
        return new TicketListVO(
                1L,
                "TK20260719182717796271518",
                1L,
                "咨询火星基地氧气补贴流程",
                status,
                LocalDateTime.of(2026, 7, 19, 18, 27, 17),
                LocalDateTime.of(2026, 7, 19, 18, 27, 17)
        );
    }
}