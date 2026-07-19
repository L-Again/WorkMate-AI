package com.workmate.ai.service.impl;

import com.workmate.ai.common.ErrorCode;
import com.workmate.ai.dto.SessionCreateDTO;
import com.workmate.ai.dto.SessionTitleUpdateDTO;
import com.workmate.ai.entity.ChatSession;
import com.workmate.ai.entity.SysUser;
import com.workmate.ai.exception.BusinessException;
import com.workmate.ai.mapper.ChatSessionMapper;
import com.workmate.ai.mapper.SysUserMapper;
import com.workmate.ai.service.ChatSessionService;
import com.workmate.ai.vo.SessionVO;
import org.springframework.stereotype.Service;
import com.workmate.ai.common.PageResult;
import com.workmate.ai.vo.SessionListVO;
import com.workmate.ai.mapper.ChatMessageMapper;
import com.workmate.ai.vo.MessageVO;

import java.util.List;

@Service
public class ChatSessionServiceImpl implements ChatSessionService {

    private static final int ENABLED_STATUS = 1;
    private static final int NOT_DELETED = 0;
    private static final int DELETED = 1;
    private static final String DEFAULT_TITLE = "新会话";

    private final SysUserMapper sysUserMapper;
    private final ChatSessionMapper chatSessionMapper;
    private final ChatMessageMapper chatMessageMapper;

    public ChatSessionServiceImpl(SysUserMapper sysUserMapper,
                                  ChatSessionMapper chatSessionMapper,
                                  ChatMessageMapper chatMessageMapper) {
        this.sysUserMapper = sysUserMapper;
        this.chatSessionMapper = chatSessionMapper;
        this.chatMessageMapper = chatMessageMapper;
    }

    @Override
    public SessionVO createSession(Long userId, SessionCreateDTO request) {
        validateEnabledUser(userId);

        ChatSession session = new ChatSession();
        session.setUserId(userId);
        session.setTitle(normalizeTitle(request.getTitle()));
        session.setIsDeleted(NOT_DELETED);

        chatSessionMapper.insert(session);

        ChatSession createdSession = chatSessionMapper.selectById(session.getId());
        return toVO(createdSession == null ? session : createdSession);
    }

    @Override
    public PageResult<SessionListVO> listSessions(Long userId, Long pageNum, Long pageSize) {
        validateEnabledUser(userId);

        long safePageNum = pageNum == null || pageNum < 1 ? 1 : pageNum;
        long safePageSize = pageSize == null || pageSize < 1 ? 20 : Math.min(pageSize, 100);
        long offset = (safePageNum - 1) * safePageSize;

        Long total = chatSessionMapper.countActiveSessions(userId);
        List<SessionListVO> records = chatSessionMapper.selectSessionPage(userId, offset, safePageSize);
        long pages = total == 0 ? 0 : (total + safePageSize - 1) / safePageSize;

        return new PageResult<>(records, safePageNum, safePageSize, total, pages);
    }

    @Override
    public PageResult<MessageVO> listMessages(Long userId, Long sessionId, Long pageNum, Long pageSize) {
        validateEnabledUser(userId);
        getOwnedActiveSession(userId, sessionId);

        long safePageNum = pageNum == null || pageNum < 1 ? 1 : pageNum;
        long safePageSize = pageSize == null || pageSize < 1 ? 50 : Math.min(pageSize, 100);
        long offset = (safePageNum - 1) * safePageSize;

        Long total = chatMessageMapper.countMessagesBySession(sessionId);
        List<MessageVO> records = chatMessageMapper.selectMessagesBySession(sessionId, offset, safePageSize);
        long pages = total == 0 ? 0 : (total + safePageSize - 1) / safePageSize;

        return new PageResult<>(records, safePageNum, safePageSize, total, pages);
    }

    @Override
    public SessionVO getSessionDetail(Long userId, Long sessionId) {
        validateEnabledUser(userId);
        ChatSession session = getOwnedActiveSession(userId, sessionId);
        return toVO(session);
    }

    @Override
    public SessionVO updateSessionTitle(Long userId, Long sessionId, SessionTitleUpdateDTO request) {
        validateEnabledUser(userId);
        ChatSession existing = getOwnedActiveSession(userId, sessionId);

        ChatSession updated = new ChatSession();
        updated.setId(sessionId);
        updated.setTitle(request.getTitle());

        chatSessionMapper.updateById(updated);

        existing.setTitle(request.getTitle());
        return toVO(existing);
    }

    @Override
    public Boolean deleteSession(Long userId, Long sessionId) {
        validateEnabledUser(userId);
        getOwnedActiveSession(userId, sessionId);

        ChatSession deleted = new ChatSession();
        deleted.setId(sessionId);
        deleted.setIsDeleted(DELETED);

        chatSessionMapper.updateById(deleted);

        return true;
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

    private String normalizeTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            return DEFAULT_TITLE;
        }
        return title.trim();
    }

    private SessionVO toVO(ChatSession session) {
        return new SessionVO(
                session.getId(),
                session.getTitle(),
                session.getLastMessageAt(),
                session.getCreatedAt()
        );
    }
}