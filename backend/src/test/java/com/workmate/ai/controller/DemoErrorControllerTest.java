package com.workmate.ai.controller;

import com.workmate.ai.common.ErrorCode;
import com.workmate.ai.exception.BusinessException;
import com.workmate.ai.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@Import({
        GlobalExceptionHandler.class,
        DemoErrorControllerTest.TestErrorController.class
})
class DemoErrorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnUnifiedResponseWhenBusinessExceptionThrown() throws Exception {
        mockMvc.perform(get("/api/demo/errors/business"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(40001)))
                .andExpect(jsonPath("$.message", is("请求参数错误")))
                .andExpect(jsonPath("$.data", nullValue()));
    }

    @RestController
    public static class TestErrorController {

        @GetMapping("/api/demo/errors/business")
        public void businessError() {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
    }
}