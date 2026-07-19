package com.workmate.ai.service.impl;

import com.workmate.ai.common.ErrorCode;
import com.workmate.ai.common.PageResult;
import com.workmate.ai.dto.ModelCallLogCreateDTO;
import com.workmate.ai.entity.ModelCallLog;
import com.workmate.ai.entity.SysUser;
import com.workmate.ai.exception.BusinessException;
import com.workmate.ai.mapper.ModelCallLogMapper;
import com.workmate.ai.mapper.SysUserMapper;
import com.workmate.ai.service.ModelCallLogService;
import com.workmate.ai.vo.ModelCallLogVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class ModelCallLogServiceImpl implements ModelCallLogService {

    private static final Logger log = LoggerFactory.getLogger(ModelCallLogServiceImpl.class);

    private static final int ENABLED_STATUS = 1;
    private static final String ADMIN_ROLE = "ADMIN";
    private static final int FROM_CACHE = 1;
    private static final int NOT_FROM_CACHE = 0;
    private static final int MAX_ERROR_MESSAGE_LENGTH = 1000;
    private static final Set<String> CALL_STATUSES = Set.of("CACHE_HIT", "SUCCESS", "FAILED", "NO_KNOWLEDGE");

    private final SysUserMapper sysUserMapper;
    private final ModelCallLogMapper modelCallLogMapper;

    public ModelCallLogServiceImpl(SysUserMapper sysUserMapper,
                                   ModelCallLogMapper modelCallLogMapper) {
        this.sysUserMapper = sysUserMapper;
        this.modelCallLogMapper = modelCallLogMapper;
    }

    @Async("modelLogExecutor")
    @Override
    public void recordAsync(ModelCallLogCreateDTO request) {
        try {
            ModelCallLog logRecord = new ModelCallLog();
            logRecord.setUserId(request.getUserId());
            logRecord.setSessionId(request.getSessionId());
            logRecord.setQuestionMessageId(request.getQuestionMessageId());
            logRecord.setAnswerMessageId(request.getAnswerMessageId());
            logRecord.setModelName(request.getModelName());
            logRecord.setFromCache(Boolean.TRUE.equals(request.getFromCache()) ? FROM_CACHE : NOT_FROM_CACHE);
            logRecord.setCallStatus(request.getCallStatus());
            logRecord.setDurationMs(request.getDurationMs() == null ? 0L : request.getDurationMs());
            logRecord.setPromptTokens(request.getPromptTokens());
            logRecord.setCompletionTokens(request.getCompletionTokens());
            logRecord.setErrorMessage(truncateErrorMessage(request.getErrorMessage()));

            modelCallLogMapper.insert(logRecord);
        } catch (RuntimeException exception) {
            log.error("Failed to record model call log asynchronously.", exception);
        }
    }

    @Override
    public PageResult<ModelCallLogVO> pageModelCallLogs(Long userId, Long pageNum, Long pageSize, String callStatus) {
        SysUser user = validateEnabledUser(userId);
        if (!ADMIN_ROLE.equals(user.getRole())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        String normalizedStatus = normalizeOptionalStatus(callStatus);
        long safePageNum = pageNum == null || pageNum < 1 ? 1 : pageNum;
        long safePageSize = pageSize == null || pageSize < 1 ? 20 : Math.min(pageSize, 100);
        long offset = (safePageNum - 1) * safePageSize;

        Long total = modelCallLogMapper.countModelCallLogs(normalizedStatus);
        List<ModelCallLogVO> records = modelCallLogMapper.selectModelCallLogPage(normalizedStatus, offset, safePageSize);
        long pages = total == 0 ? 0 : (total + safePageSize - 1) / safePageSize;

        return new PageResult<>(records, safePageNum, safePageSize, total, pages);
    }

    private SysUser validateEnabledUser(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null || !Integer.valueOf(ENABLED_STATUS).equals(user.getStatus())) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND_OR_DISABLED);
        }
        return user;
    }

    private String normalizeOptionalStatus(String callStatus) {
        if (callStatus == null || callStatus.trim().isEmpty()) {
            return null;
        }

        String normalized = callStatus.trim().toUpperCase();
        if (!CALL_STATUSES.contains(normalized)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        return normalized;
    }

    private String truncateErrorMessage(String errorMessage) {
        if (errorMessage == null) {
            return null;
        }
        String trimmed = errorMessage.trim();
        if (trimmed.length() <= MAX_ERROR_MESSAGE_LENGTH) {
            return trimmed;
        }
        return trimmed.substring(0, MAX_ERROR_MESSAGE_LENGTH);
    }
}