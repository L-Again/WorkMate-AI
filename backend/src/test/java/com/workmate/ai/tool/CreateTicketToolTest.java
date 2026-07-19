package com.workmate.ai.tool;

import com.workmate.ai.common.ErrorCode;
import com.workmate.ai.dto.TicketCreateDTO;
import com.workmate.ai.exception.BusinessException;
import com.workmate.ai.service.TicketService;
import com.workmate.ai.tool.impl.CreateTicketTool;
import com.workmate.ai.vo.TicketVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class CreateTicketToolTest {

    @Mock
    private TicketService ticketService;

    private CreateTicketTool createTicketTool;

    @BeforeEach
    void setUp() {
        createTicketTool = new CreateTicketTool(ticketService);
    }

    @Test
    void shouldExecuteCreateTicketWhenConfirmed() {
        TicketVO ticketVO = new TicketVO(
                1L,
                "TK20260719182717796271518",
                1L,
                9L,
                11L,
                "咨询火星基地氧气补贴流程",
                "Agent 未找到相关知识，希望人工确认。",
                "PENDING",
                null,
                null,
                null,
                LocalDateTime.of(2026, 7, 19, 18, 27, 17),
                LocalDateTime.of(2026, 7, 19, 18, 27, 17)
        );

        when(ticketService.createTicket(any(), any(TicketCreateDTO.class))).thenReturn(ticketVO);

        Object result = createTicketTool.execute(Map.of(
                "confirmed", true,
                "userId", 1L,
                "sessionId", 9L,
                "questionMessageId", 11L,
                "title", " 咨询火星基地氧气补贴流程 ",
                "description", " Agent 未找到相关知识，希望人工确认。 "
        ));

        assertThat(result).isEqualTo(ticketVO);

        ArgumentCaptor<TicketCreateDTO> requestCaptor = ArgumentCaptor.forClass(TicketCreateDTO.class);
        verify(ticketService).createTicket(org.mockito.ArgumentMatchers.eq(1L), requestCaptor.capture());

        TicketCreateDTO request = requestCaptor.getValue();
        assertThat(request.getSessionId()).isEqualTo(9L);
        assertThat(request.getQuestionMessageId()).isEqualTo(11L);
        assertThat(request.getTitle()).isEqualTo("咨询火星基地氧气补贴流程");
        assertThat(request.getDescription()).isEqualTo("Agent 未找到相关知识，希望人工确认。");
    }

    @Test
    void shouldRejectCreateTicketWhenNotConfirmed() {
        assertThatThrownBy(() -> createTicketTool.execute(Map.of(
                "confirmed", false,
                "userId", 1L,
                "sessionId", 9L,
                "questionMessageId", 11L,
                "title", "咨询火星基地氧气补贴流程",
                "description", "Agent 未找到相关知识，希望人工确认。"
        )))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.STATE_CONFLICT));

        verify(ticketService, never()).createTicket(any(), any());
    }

    @Test
    void shouldRejectCreateTicketWhenConfirmedIsMissing() {
        assertThatThrownBy(() -> createTicketTool.execute(Map.of(
                "userId", 1L,
                "sessionId", 9L,
                "questionMessageId", 11L,
                "title", "咨询火星基地氧气补贴流程",
                "description", "Agent 未找到相关知识，希望人工确认。"
        )))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.PARAM_ERROR));

        verify(ticketService, never()).createTicket(any(), any());
    }
}