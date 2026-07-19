package com.workmate.ai.service;

import com.workmate.ai.common.ErrorCode;
import com.workmate.ai.dto.SessionCreateDTO;
import com.workmate.ai.dto.SessionTitleUpdateDTO;
import com.workmate.ai.entity.ChatSession;
import com.workmate.ai.entity.SysUser;
import com.workmate.ai.exception.BusinessException;
import com.workmate.ai.mapper.ChatSessionMapper;
import com.workmate.ai.mapper.SysUserMapper;
import com.workmate.ai.service.impl.ChatSessionServiceImpl;
import com.workmate.ai.vo.SessionVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.workmate.ai.common.PageResult;
import com.workmate.ai.vo.SessionListVO;

import java.util.List;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatSessionServiceTest {

    @Mock
    private SysUserMapper sysUserMapper;

    @Mock
    private ChatSessionMapper chatSessionMapper;

    private ChatSessionService chatSessionService;

    @BeforeEach
    void setUp() {
        chatSessionService = new ChatSessionServiceImpl(sysUserMapper, chatSessionMapper);
    }

    @Test
    void shouldCreateSession() {
        SessionCreateDTO request = new SessionCreateDTO();
        request.setTitle(" Git 规范咨询 ");

        when(sysUserMapper.selectById(1L)).thenReturn(user(1L, 1));
        when(chatSessionMapper.insert(any(ChatSession.class))).thenAnswer(invocation -> {
            ChatSession session = invocation.getArgument(0);
            session.setId(10L);
            return 1;
        });
        when(chatSessionMapper.selectById(10L))
                .thenReturn(session(10L, 1L, "Git 规范咨询", 0));

        SessionVO result = chatSessionService.createSession(1L, request);

        assertThat(result.getSessionId()).isEqualTo(10L);
        assertThat(result.getTitle()).isEqualTo("Git 规范咨询");

        ArgumentCaptor<ChatSession> captor = ArgumentCaptor.forClass(ChatSession.class);
        verify(chatSessionMapper).insert(captor.capture());

        ChatSession inserted = captor.getValue();
        assertThat(inserted.getUserId()).isEqualTo(1L);
        assertThat(inserted.getTitle()).isEqualTo("Git 规范咨询");
        assertThat(inserted.getIsDeleted()).isEqualTo(0);
    }

    @Test
    void shouldListSessionsWithEmptySession() {
        when(sysUserMapper.selectById(1L)).thenReturn(user(1L, 1));

        SessionListVO emptySession = new SessionListVO(
                10L,
                "空会话",
                null,
                null,
                LocalDateTime.of(2026, 7, 19, 11, 40)
        );

        when(chatSessionMapper.countActiveSessions(1L)).thenReturn(1L);
        when(chatSessionMapper.selectSessionPage(1L, 0L, 20L))
                .thenReturn(List.of(emptySession));

        PageResult<SessionListVO> result = chatSessionService.listSessions(1L, 1L, 20L);

        assertThat(result.getPageNum()).isEqualTo(1L);
        assertThat(result.getPageSize()).isEqualTo(20L);
        assertThat(result.getTotal()).isEqualTo(1L);
        assertThat(result.getPages()).isEqualTo(1L);
        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getRecords().get(0).getSessionId()).isEqualTo(10L);
        assertThat(result.getRecords().get(0).getTitle()).isEqualTo("空会话");
        assertThat(result.getRecords().get(0).getLastMessage()).isNull();
    }

    @Test
    void shouldReturnUserNotFoundWhenListingSessionsWithMissingUser() {
        when(sysUserMapper.selectById(999L)).thenReturn(null);

        assertThatThrownBy(() -> chatSessionService.listSessions(999L, 1L, 20L))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND_OR_DISABLED));

        verify(chatSessionMapper, never()).countActiveSessions(any());
        verify(chatSessionMapper, never()).selectSessionPage(any(), any(), any());
    }

    @Test
    void shouldGetOwnSessionDetail() {
        when(sysUserMapper.selectById(1L)).thenReturn(user(1L, 1));

        ChatSession session = session(10L, 1L, "Git 规范咨询", 0);
        when(chatSessionMapper.selectById(10L)).thenReturn(session);

        SessionVO result = chatSessionService.getSessionDetail(1L, 10L);

        assertThat(result.getSessionId()).isEqualTo(10L);
        assertThat(result.getTitle()).isEqualTo("Git 规范咨询");
    }

    @Test
    void shouldReturnNotFoundWhenGettingOtherUserSession() {
        when(sysUserMapper.selectById(1L)).thenReturn(user(1L, 1));

        ChatSession session = session(10L, 2L, "别人的会话", 0);
        when(chatSessionMapper.selectById(10L)).thenReturn(session);

        assertThatThrownBy(() -> chatSessionService.getSessionDetail(1L, 10L))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.DATA_NOT_FOUND));
    }

    @Test
    void shouldUpdateOwnSessionTitle() {
        SessionTitleUpdateDTO request = new SessionTitleUpdateDTO();
        request.setTitle("Git 与代码提交规范");

        when(sysUserMapper.selectById(1L)).thenReturn(user(1L, 1));

        ChatSession session = session(10L, 1L, "Git 规范咨询", 0);
        when(chatSessionMapper.selectById(10L)).thenReturn(session);
        when(chatSessionMapper.updateById(any(ChatSession.class))).thenReturn(1);

        SessionVO result = chatSessionService.updateSessionTitle(1L, 10L, request);

        assertThat(result.getTitle()).isEqualTo("Git 与代码提交规范");

        ArgumentCaptor<ChatSession> captor = ArgumentCaptor.forClass(ChatSession.class);
        verify(chatSessionMapper).updateById(captor.capture());

        ChatSession updated = captor.getValue();
        assertThat(updated.getId()).isEqualTo(10L);
        assertThat(updated.getTitle()).isEqualTo("Git 与代码提交规范");
    }

    @Test
    void shouldDeleteOwnSession() {
        when(sysUserMapper.selectById(1L)).thenReturn(user(1L, 1));

        ChatSession session = session(10L, 1L, "Git 规范咨询", 0);
        when(chatSessionMapper.selectById(10L)).thenReturn(session);
        when(chatSessionMapper.updateById(any(ChatSession.class))).thenReturn(1);

        Boolean result = chatSessionService.deleteSession(1L, 10L);

        assertThat(result).isTrue();

        ArgumentCaptor<ChatSession> captor = ArgumentCaptor.forClass(ChatSession.class);
        verify(chatSessionMapper).updateById(captor.capture());

        ChatSession deleted = captor.getValue();
        assertThat(deleted.getId()).isEqualTo(10L);
        assertThat(deleted.getIsDeleted()).isEqualTo(1);
    }

    @Test
    void shouldReturnUserNotFoundWhenCreatingSessionWithMissingUser() {
        SessionCreateDTO request = new SessionCreateDTO();
        request.setTitle("Git 规范咨询");

        when(sysUserMapper.selectById(999L)).thenReturn(null);

        assertThatThrownBy(() -> chatSessionService.createSession(999L, request))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND_OR_DISABLED));

        verify(chatSessionMapper, never()).insert(any(ChatSession.class));
    }

    private SysUser user(Long id, Integer status) {
        SysUser user = new SysUser();
        user.setId(id);
        user.setStatus(status);
        return user;
    }

    private ChatSession session(Long id, Long userId, String title, Integer isDeleted) {
        ChatSession session = new ChatSession();
        session.setId(id);
        session.setUserId(userId);
        session.setTitle(title);
        session.setIsDeleted(isDeleted);
        session.setCreatedAt(LocalDateTime.of(2026, 7, 19, 11, 40));
        return session;
    }
}