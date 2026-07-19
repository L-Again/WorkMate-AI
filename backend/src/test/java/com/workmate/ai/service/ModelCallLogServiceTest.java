package com.workmate.ai.service;

import com.workmate.ai.common.ErrorCode;
import com.workmate.ai.common.PageResult;
import com.workmate.ai.dto.ModelCallLogCreateDTO;
import com.workmate.ai.entity.ModelCallLog;
import com.workmate.ai.entity.SysUser;
import com.workmate.ai.exception.BusinessException;
import com.workmate.ai.mapper.ModelCallLogMapper;
import com.workmate.ai.mapper.SysUserMapper;
import com.workmate.ai.service.impl.ModelCallLogServiceImpl;
import com.workmate.ai.vo.ModelCallLogVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ModelCallLogServiceTest {

    @Mock
    private SysUserMapper sysUserMapper;

    @Mock
    private ModelCallLogMapper modelCallLogMapper;

    private ModelCallLogService modelCallLogService;

    @BeforeEach
    void setUp() {
        modelCallLogService = new ModelCallLogServiceImpl(sysUserMapper, modelCallLogMapper);
    }

    @Test
    void shouldRecordModelCallLog() {
        ModelCallLogCreateDTO request = new ModelCallLogCreateDTO();
        request.setUserId(1L);
        request.setSessionId(10L);
        request.setQuestionMessageId(101L);
        request.setAnswerMessageId(102L);
        request.setModelName("mock-llm");
        request.setFromCache(false);
        request.setCallStatus("SUCCESS");
        request.setDurationMs(120L);
        request.setPromptTokens(30);
        request.setCompletionTokens(60);

        modelCallLogService.recordAsync(request);

        ArgumentCaptor<ModelCallLog> captor = ArgumentCaptor.forClass(ModelCallLog.class);
        verify(modelCallLogMapper).insert(captor.capture());

        ModelCallLog inserted = captor.getValue();
        assertThat(inserted.getUserId()).isEqualTo(1L);
        assertThat(inserted.getSessionId()).isEqualTo(10L);
        assertThat(inserted.getQuestionMessageId()).isEqualTo(101L);
        assertThat(inserted.getAnswerMessageId()).isEqualTo(102L);
        assertThat(inserted.getModelName()).isEqualTo("mock-llm");
        assertThat(inserted.getFromCache()).isEqualTo(0);
        assertThat(inserted.getCallStatus()).isEqualTo("SUCCESS");
        assertThat(inserted.getDurationMs()).isEqualTo(120L);
        assertThat(inserted.getPromptTokens()).isEqualTo(30);
        assertThat(inserted.getCompletionTokens()).isEqualTo(60);
    }

    @Test
    void shouldRecordCacheHitLog() {
        ModelCallLogCreateDTO request = new ModelCallLogCreateDTO();
        request.setUserId(1L);
        request.setSessionId(10L);
        request.setQuestionMessageId(401L);
        request.setAnswerMessageId(402L);
        request.setFromCache(true);
        request.setCallStatus("CACHE_HIT");
        request.setDurationMs(null);

        modelCallLogService.recordAsync(request);

        ArgumentCaptor<ModelCallLog> captor = ArgumentCaptor.forClass(ModelCallLog.class);
        verify(modelCallLogMapper).insert(captor.capture());

        ModelCallLog inserted = captor.getValue();
        assertThat(inserted.getFromCache()).isEqualTo(1);
        assertThat(inserted.getCallStatus()).isEqualTo("CACHE_HIT");
        assertThat(inserted.getDurationMs()).isEqualTo(0L);
        assertThat(inserted.getModelName()).isNull();
    }

    @Test
    void shouldNotThrowWhenModelLogInsertFails() {
        ModelCallLogCreateDTO request = new ModelCallLogCreateDTO();
        request.setUserId(1L);
        request.setSessionId(10L);
        request.setCallStatus("FAILED");

        when(modelCallLogMapper.insert(any(ModelCallLog.class)))
                .thenThrow(new RuntimeException("database unavailable"));

        assertThatCode(() -> modelCallLogService.recordAsync(request))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldPageModelCallLogsWhenUserIsAdmin() {
        when(sysUserMapper.selectById(2L)).thenReturn(user(2L, "ADMIN", 1));
        when(modelCallLogMapper.countModelCallLogs("SUCCESS")).thenReturn(1L);
        when(modelCallLogMapper.selectModelCallLogPage("SUCCESS", 0L, 20L))
                .thenReturn(List.of(new ModelCallLogVO(
                        1L,
                        1L,
                        10L,
                        101L,
                        102L,
                        "mock-llm",
                        false,
                        "SUCCESS",
                        120L,
                        30,
                        60,
                        null,
                        LocalDateTime.of(2026, 7, 19, 19, 0)
                )));

        PageResult<ModelCallLogVO> result = modelCallLogService.pageModelCallLogs(2L, 1L, 20L, "success");

        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getRecords().get(0).getCallStatus()).isEqualTo("SUCCESS");
        assertThat(result.getRecords().get(0).getModelName()).isEqualTo("mock-llm");
        assertThat(result.getTotal()).isEqualTo(1L);
        assertThat(result.getPages()).isEqualTo(1L);
    }

    @Test
    void shouldRejectModelLogPageWhenUserIsEmployee() {
        when(sysUserMapper.selectById(1L)).thenReturn(user(1L, "EMPLOYEE", 1));

        assertThatThrownBy(() -> modelCallLogService.pageModelCallLogs(1L, 1L, 20L, null))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN));

        verify(modelCallLogMapper, never()).countModelCallLogs(any());
        verify(modelCallLogMapper, never()).selectModelCallLogPage(any(), any(), any());
    }

    @Test
    void shouldRejectInvalidCallStatus() {
        when(sysUserMapper.selectById(2L)).thenReturn(user(2L, "ADMIN", 1));

        assertThatThrownBy(() -> modelCallLogService.pageModelCallLogs(2L, 1L, 20L, "UNKNOWN"))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.PARAM_ERROR));

        verify(modelCallLogMapper, never()).countModelCallLogs(any());
        verify(modelCallLogMapper, never()).selectModelCallLogPage(any(), any(), any());
    }

    private SysUser user(Long id, String role, Integer status) {
        SysUser user = new SysUser();
        user.setId(id);
        user.setRole(role);
        user.setStatus(status);
        return user;
    }
}