package com.workmate.ai.controller;

import com.workmate.ai.common.ErrorCode;
import com.workmate.ai.common.PageResult;
import com.workmate.ai.exception.BusinessException;
import com.workmate.ai.exception.GlobalExceptionHandler;
import com.workmate.ai.service.ModelCallLogService;
import com.workmate.ai.vo.ModelCallLogVO;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ModelCallLogController.class)
@Import(GlobalExceptionHandler.class)
class ModelCallLogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ModelCallLogService modelCallLogService;

    @Test
    void shouldPageModelCallLogs() throws Exception {
        when(modelCallLogService.pageModelCallLogs(2L, 1L, 20L, "SUCCESS"))
                .thenReturn(new PageResult<>(
                        List.of(new ModelCallLogVO(
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
                        )),
                        1L,
                        20L,
                        1L,
                        1L
                ));

        mockMvc.perform(get("/api/admin/model-logs")
                        .header("X-User-Id", "2")
                        .param("pageNum", "1")
                        .param("pageSize", "20")
                        .param("callStatus", "SUCCESS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(200)))
                .andExpect(jsonPath("$.data.records", hasSize(1)))
                .andExpect(jsonPath("$.data.records[0].id", is(1)))
                .andExpect(jsonPath("$.data.records[0].modelName", is("mock-llm")))
                .andExpect(jsonPath("$.data.records[0].fromCache", is(false)))
                .andExpect(jsonPath("$.data.records[0].callStatus", is("SUCCESS")))
                .andExpect(jsonPath("$.data.total", is(1)));
    }

    @Test
    void shouldReturnForbiddenWhenEmployeePagesModelCallLogs() throws Exception {
        when(modelCallLogService.pageModelCallLogs(1L, 1L, 20L, null))
                .thenThrow(new BusinessException(ErrorCode.FORBIDDEN));

        mockMvc.perform(get("/api/admin/model-logs")
                        .header("X-User-Id", "1")
                        .param("pageNum", "1")
                        .param("pageSize", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(40301)))
                .andExpect(jsonPath("$.message", is("无权限")));
    }
}