package com.workmate.ai.service;

import com.workmate.ai.common.ErrorCode;
import com.workmate.ai.common.PageResult;
import com.workmate.ai.dto.TicketCreateDTO;
import com.workmate.ai.dto.TicketStatusUpdateDTO;
import com.workmate.ai.entity.ChatMessage;
import com.workmate.ai.entity.ChatSession;
import com.workmate.ai.entity.SysUser;
import com.workmate.ai.entity.Ticket;
import com.workmate.ai.exception.BusinessException;
import com.workmate.ai.mapper.ChatMessageMapper;
import com.workmate.ai.mapper.ChatSessionMapper;
import com.workmate.ai.mapper.SysUserMapper;
import com.workmate.ai.mapper.TicketMapper;
import com.workmate.ai.service.impl.TicketServiceImpl;
import com.workmate.ai.vo.TicketListVO;
import com.workmate.ai.vo.TicketVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private SysUserMapper sysUserMapper;

    @Mock
    private ChatSessionMapper chatSessionMapper;

    @Mock
    private ChatMessageMapper chatMessageMapper;

    @Mock
    private TicketMapper ticketMapper;

    private TicketService ticketService;

    @BeforeEach
    void setUp() {
        ticketService = new TicketServiceImpl(
                sysUserMapper,
                chatSessionMapper,
                chatMessageMapper,
                ticketMapper
        );
    }

    @Test
    void shouldCreateTicketFromOwnUserQuestionMessage() {
        TicketCreateDTO request = createRequest();

        when(sysUserMapper.selectById(1L)).thenReturn(user(1L, "EMPLOYEE", 1));
        when(chatSessionMapper.selectById(9L)).thenReturn(session(9L, 1L, 0));
        when(chatMessageMapper.selectById(11L)).thenReturn(message(11L, 9L, 1L, "USER", 0));
        when(ticketMapper.insert(any(Ticket.class))).thenAnswer(invocation -> {
            Ticket ticket = invocation.getArgument(0);
            ticket.setId(1L);
            return 1;
        });
        when(ticketMapper.selectById(1L)).thenReturn(ticket(1L, 1L, 9L, 11L, "PENDING"));

        TicketVO result = ticketService.createTicket(1L, request);

        assertThat(result.getTicketId()).isEqualTo(1L);
        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getSessionId()).isEqualTo(9L);
        assertThat(result.getQuestionMessageId()).isEqualTo(11L);
        assertThat(result.getStatus()).isEqualTo("PENDING");

        ArgumentCaptor<Ticket> captor = ArgumentCaptor.forClass(Ticket.class);
        verify(ticketMapper).insert(captor.capture());

        Ticket inserted = captor.getValue();
        assertThat(inserted.getTicketNo()).startsWith("TK");
        assertThat(inserted.getUserId()).isEqualTo(1L);
        assertThat(inserted.getSessionId()).isEqualTo(9L);
        assertThat(inserted.getQuestionMessageId()).isEqualTo(11L);
        assertThat(inserted.getTitle()).isEqualTo("咨询火星基地氧气补贴流程");
        assertThat(inserted.getDescription()).isEqualTo("Agent 未找到相关知识，希望人工确认。");
        assertThat(inserted.getStatus()).isEqualTo("PENDING");
        assertThat(inserted.getIsDeleted()).isEqualTo(0);
    }

    @Test
    void shouldRejectCreateTicketWhenSessionBelongsToOtherUser() {
        TicketCreateDTO request = createRequest();

        when(sysUserMapper.selectById(1L)).thenReturn(user(1L, "EMPLOYEE", 1));
        when(chatSessionMapper.selectById(9L)).thenReturn(session(9L, 2L, 0));

        assertThatThrownBy(() -> ticketService.createTicket(1L, request))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.DATA_NOT_FOUND));

        verify(chatMessageMapper, never()).selectById(any());
        verify(ticketMapper, never()).insert(any(Ticket.class));
    }

    @Test
    void shouldRejectCreateTicketWhenSourceMessageIsNotUserMessage() {
        TicketCreateDTO request = createRequest();

        when(sysUserMapper.selectById(1L)).thenReturn(user(1L, "EMPLOYEE", 1));
        when(chatSessionMapper.selectById(9L)).thenReturn(session(9L, 1L, 0));
        when(chatMessageMapper.selectById(11L)).thenReturn(message(11L, 9L, 1L, "ASSISTANT", 0));

        assertThatThrownBy(() -> ticketService.createTicket(1L, request))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.DATA_NOT_FOUND));

        verify(ticketMapper, never()).insert(any(Ticket.class));
    }

    @Test
    void shouldListMyTickets() {
        when(sysUserMapper.selectById(1L)).thenReturn(user(1L, "EMPLOYEE", 1));
        when(ticketMapper.countMyTickets(1L, "PENDING")).thenReturn(1L);
        when(ticketMapper.selectMyTicketPage(1L, "PENDING", 0L, 10L))
                .thenReturn(List.of(new TicketListVO(
                        1L,
                        "TK20260719182717796271518",
                        1L,
                        "咨询火星基地氧气补贴流程",
                        "PENDING",
                        LocalDateTime.of(2026, 7, 19, 18, 27, 17),
                        LocalDateTime.of(2026, 7, 19, 18, 27, 17)
                )));

        PageResult<TicketListVO> result = ticketService.listMyTickets(1L, 1L, 10L, "PENDING");

        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getRecords().get(0).getTicketId()).isEqualTo(1L);
        assertThat(result.getTotal()).isEqualTo(1L);
        assertThat(result.getPages()).isEqualTo(1L);
    }

    @Test
    void shouldRejectAdminTicketListWhenUserIsEmployee() {
        when(sysUserMapper.selectById(1L)).thenReturn(user(1L, "EMPLOYEE", 1));

        assertThatThrownBy(() -> ticketService.listAdminTickets(1L, 1L, 10L, null, null))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN));

        verify(ticketMapper, never()).countAdminTickets(any(), any());
        verify(ticketMapper, never()).selectAdminTicketPage(any(), any(), any(), any());
    }

    @Test
    void shouldUpdateTicketFromProcessingToResolved() {
        TicketStatusUpdateDTO request = new TicketStatusUpdateDTO();
        request.setStatus("RESOLVED");
        request.setResolution("由项目负责人提交资产申请，审批后由 IT 配置。");

        when(sysUserMapper.selectById(2L)).thenReturn(user(2L, "ADMIN", 1));
        when(ticketMapper.selectById(1L))
                .thenReturn(ticket(1L, 1L, 9L, 11L, "PROCESSING"))
                .thenReturn(resolvedTicket());

        TicketVO result = ticketService.updateTicketStatus(2L, 1L, request);

        assertThat(result.getTicketId()).isEqualTo(1L);
        assertThat(result.getStatus()).isEqualTo("RESOLVED");
        assertThat(result.getResolution()).isEqualTo("由项目负责人提交资产申请，审批后由 IT 配置。");
        assertThat(result.getHandledBy()).isEqualTo(2L);

        ArgumentCaptor<Ticket> captor = ArgumentCaptor.forClass(Ticket.class);
        verify(ticketMapper).updateById(captor.capture());

        Ticket updated = captor.getValue();
        assertThat(updated.getId()).isEqualTo(1L);
        assertThat(updated.getStatus()).isEqualTo("RESOLVED");
        assertThat(updated.getResolution()).isEqualTo("由项目负责人提交资产申请，审批后由 IT 配置。");
        assertThat(updated.getHandledBy()).isEqualTo(2L);
        assertThat(updated.getResolvedAt()).isNotNull();
    }

    @Test
    void shouldRejectInvalidTicketRollback() {
        TicketStatusUpdateDTO request = new TicketStatusUpdateDTO();
        request.setStatus("PROCESSING");

        when(sysUserMapper.selectById(2L)).thenReturn(user(2L, "ADMIN", 1));
        when(ticketMapper.selectById(1L)).thenReturn(ticket(1L, 1L, 9L, 11L, "RESOLVED"));

        assertThatThrownBy(() -> ticketService.updateTicketStatus(2L, 1L, request))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.STATE_CONFLICT));

        verify(ticketMapper, never()).updateById(any(Ticket.class));
    }

    private TicketCreateDTO createRequest() {
        TicketCreateDTO request = new TicketCreateDTO();
        request.setSessionId(9L);
        request.setQuestionMessageId(11L);
        request.setTitle(" 咨询火星基地氧气补贴流程 ");
        request.setDescription(" Agent 未找到相关知识，希望人工确认。 ");
        return request;
    }

    private SysUser user(Long id, String role, Integer status) {
        SysUser user = new SysUser();
        user.setId(id);
        user.setRole(role);
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

    private ChatMessage message(Long id, Long sessionId, Long userId, String role, Integer isDeleted) {
        ChatMessage message = new ChatMessage();
        message.setId(id);
        message.setSessionId(sessionId);
        message.setUserId(userId);
        message.setRole(role);
        message.setIsDeleted(isDeleted);
        return message;
    }

    private Ticket ticket(Long id, Long userId, Long sessionId, Long questionMessageId, String status) {
        Ticket ticket = new Ticket();
        ticket.setId(id);
        ticket.setTicketNo("TK20260719182717796271518");
        ticket.setUserId(userId);
        ticket.setSessionId(sessionId);
        ticket.setQuestionMessageId(questionMessageId);
        ticket.setTitle("咨询火星基地氧气补贴流程");
        ticket.setDescription("Agent 未找到相关知识，希望人工确认。");
        ticket.setStatus(status);
        ticket.setIsDeleted(0);
        ticket.setCreatedAt(LocalDateTime.of(2026, 7, 19, 18, 27, 17));
        ticket.setUpdatedAt(LocalDateTime.of(2026, 7, 19, 18, 27, 17));
        return ticket;
    }

    private Ticket resolvedTicket() {
        Ticket ticket = ticket(1L, 1L, 9L, 11L, "RESOLVED");
        ticket.setResolution("由项目负责人提交资产申请，审批后由 IT 配置。");
        ticket.setHandledBy(2L);
        ticket.setResolvedAt(LocalDateTime.of(2026, 7, 19, 18, 31, 49));
        return ticket;
    }
}