package com.workmate.ai.service.impl;

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
import com.workmate.ai.service.TicketService;
import com.workmate.ai.vo.TicketListVO;
import com.workmate.ai.vo.TicketVO;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class TicketServiceImpl implements TicketService {

    private static final int ENABLED_STATUS = 1;
    private static final int NOT_DELETED = 0;
    private static final String ADMIN_ROLE = "ADMIN";
    private static final String USER_MESSAGE_ROLE = "USER";

    private static final String PENDING = "PENDING";
    private static final String PROCESSING = "PROCESSING";
    private static final String RESOLVED = "RESOLVED";
    private static final String CLOSED = "CLOSED";
    private static final Set<String> TICKET_STATUSES = Set.of(PENDING, PROCESSING, RESOLVED, CLOSED);

    private static final DateTimeFormatter TICKET_NO_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private final SysUserMapper sysUserMapper;
    private final ChatSessionMapper chatSessionMapper;
    private final ChatMessageMapper chatMessageMapper;
    private final TicketMapper ticketMapper;

    public TicketServiceImpl(SysUserMapper sysUserMapper,
                             ChatSessionMapper chatSessionMapper,
                             ChatMessageMapper chatMessageMapper,
                             TicketMapper ticketMapper) {
        this.sysUserMapper = sysUserMapper;
        this.chatSessionMapper = chatSessionMapper;
        this.chatMessageMapper = chatMessageMapper;
        this.ticketMapper = ticketMapper;
    }

    @Override
    public TicketVO createTicket(Long userId, TicketCreateDTO request) {
        validateEnabledUser(userId);
        validateOwnedActiveSession(userId, request.getSessionId());
        validateQuestionMessage(userId, request.getSessionId(), request.getQuestionMessageId());

        Ticket ticket = new Ticket();
        ticket.setTicketNo(generateTicketNo());
        ticket.setUserId(userId);
        ticket.setSessionId(request.getSessionId());
        ticket.setQuestionMessageId(request.getQuestionMessageId());
        ticket.setTitle(request.getTitle().trim());
        ticket.setDescription(request.getDescription().trim());
        ticket.setStatus(PENDING);
        ticket.setIsDeleted(NOT_DELETED);

        ticketMapper.insert(ticket);

        Ticket createdTicket = ticketMapper.selectById(ticket.getId());
        return toVO(createdTicket == null ? ticket : createdTicket);
    }

    @Override
    public PageResult<TicketListVO> listMyTickets(Long userId, Long pageNum, Long pageSize, String status) {
        validateEnabledUser(userId);
        String normalizedStatus = normalizeOptionalStatus(status);

        long safePageNum = pageNum == null || pageNum < 1 ? 1 : pageNum;
        long safePageSize = pageSize == null || pageSize < 1 ? 10 : Math.min(pageSize, 100);
        long offset = (safePageNum - 1) * safePageSize;

        Long total = ticketMapper.countMyTickets(userId, normalizedStatus);
        List<TicketListVO> records = ticketMapper.selectMyTicketPage(userId, normalizedStatus, offset, safePageSize);
        long pages = total == 0 ? 0 : (total + safePageSize - 1) / safePageSize;

        return new PageResult<>(records, safePageNum, safePageSize, total, pages);
    }

    @Override
    public TicketVO getTicketDetail(Long userId, Long ticketId) {
        SysUser user = validateEnabledUser(userId);
        Ticket ticket = getActiveTicket(ticketId);

        if (!ADMIN_ROLE.equals(user.getRole()) && !userId.equals(ticket.getUserId())) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND);
        }

        return toVO(ticket);
    }

    @Override
    public PageResult<TicketListVO> listAdminTickets(Long userId, Long pageNum, Long pageSize,
                                                     String status, String keyword) {
        SysUser user = validateEnabledUser(userId);
        validateAdmin(user);

        String normalizedStatus = normalizeOptionalStatus(status);
        String normalizedKeyword = normalizeOptionalKeyword(keyword);

        long safePageNum = pageNum == null || pageNum < 1 ? 1 : pageNum;
        long safePageSize = pageSize == null || pageSize < 1 ? 10 : Math.min(pageSize, 100);
        long offset = (safePageNum - 1) * safePageSize;

        Long total = ticketMapper.countAdminTickets(normalizedStatus, normalizedKeyword);
        List<TicketListVO> records = ticketMapper.selectAdminTicketPage(
                normalizedStatus,
                normalizedKeyword,
                offset,
                safePageSize
        );
        long pages = total == 0 ? 0 : (total + safePageSize - 1) / safePageSize;

        return new PageResult<>(records, safePageNum, safePageSize, total, pages);
    }

    @Override
    public TicketVO updateTicketStatus(Long userId, Long ticketId, TicketStatusUpdateDTO request) {
        SysUser user = validateEnabledUser(userId);
        validateAdmin(user);

        Ticket existing = getActiveTicket(ticketId);
        String targetStatus = normalizeRequiredStatus(request.getStatus());
        validateTransition(existing.getStatus(), targetStatus);

        Ticket updated = new Ticket();
        updated.setId(ticketId);
        updated.setStatus(targetStatus);

        if (RESOLVED.equals(targetStatus)) {
            String resolution = normalizeRequiredResolution(request.getResolution());
            updated.setResolution(resolution);
            updated.setHandledBy(userId);
            updated.setResolvedAt(LocalDateTime.now());
            existing.setResolution(resolution);
            existing.setHandledBy(userId);
            existing.setResolvedAt(updated.getResolvedAt());
        }

        ticketMapper.updateById(updated);

        existing.setStatus(targetStatus);
        Ticket latest = ticketMapper.selectById(ticketId);
        return toVO(latest == null ? existing : latest);
    }

    private SysUser validateEnabledUser(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null || !Integer.valueOf(ENABLED_STATUS).equals(user.getStatus())) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND_OR_DISABLED);
        }
        return user;
    }

    private void validateAdmin(SysUser user) {
        if (!ADMIN_ROLE.equals(user.getRole())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }

    private void validateOwnedActiveSession(Long userId, Long sessionId) {
        ChatSession session = chatSessionMapper.selectById(sessionId);
        if (session == null
                || !Integer.valueOf(NOT_DELETED).equals(session.getIsDeleted())
                || !userId.equals(session.getUserId())) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND);
        }
    }

    private void validateQuestionMessage(Long userId, Long sessionId, Long questionMessageId) {
        ChatMessage message = chatMessageMapper.selectById(questionMessageId);
        if (message == null
                || !Integer.valueOf(NOT_DELETED).equals(message.getIsDeleted())
                || !userId.equals(message.getUserId())
                || !sessionId.equals(message.getSessionId())
                || !USER_MESSAGE_ROLE.equals(message.getRole())) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND);
        }
    }

    private Ticket getActiveTicket(Long ticketId) {
        Ticket ticket = ticketMapper.selectById(ticketId);
        if (ticket == null || !Integer.valueOf(NOT_DELETED).equals(ticket.getIsDeleted())) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND);
        }
        return ticket;
    }

    private String normalizeOptionalStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return null;
        }
        return normalizeRequiredStatus(status);
    }

    private String normalizeRequiredStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        String normalized = status.trim().toUpperCase();
        if (!TICKET_STATUSES.contains(normalized)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        return normalized;
    }

    private String normalizeOptionalKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return null;
        }
        return keyword.trim();
    }

    private String normalizeRequiredResolution(String resolution) {
        if (resolution == null || resolution.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        return resolution.trim();
    }

    private void validateTransition(String currentStatus, String targetStatus) {
        if (currentStatus.equals(targetStatus)) {
            throw new BusinessException(ErrorCode.STATE_CONFLICT);
        }

        boolean allowed = (PENDING.equals(currentStatus) && PROCESSING.equals(targetStatus))
                || (PENDING.equals(currentStatus) && RESOLVED.equals(targetStatus))
                || (PROCESSING.equals(currentStatus) && RESOLVED.equals(targetStatus))
                || (RESOLVED.equals(currentStatus) && CLOSED.equals(targetStatus));

        if (!allowed) {
            throw new BusinessException(ErrorCode.STATE_CONFLICT);
        }
    }

    private String generateTicketNo() {
        int random = ThreadLocalRandom.current().nextInt(100000, 1000000);
        return "TK" + LocalDateTime.now().format(TICKET_NO_TIME_FORMATTER) + random;
    }

    private TicketVO toVO(Ticket ticket) {
        return new TicketVO(
                ticket.getId(),
                ticket.getTicketNo(),
                ticket.getUserId(),
                ticket.getSessionId(),
                ticket.getQuestionMessageId(),
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getStatus(),
                ticket.getResolution(),
                ticket.getHandledBy(),
                ticket.getResolvedAt(),
                ticket.getCreatedAt(),
                ticket.getUpdatedAt()
        );
    }
}