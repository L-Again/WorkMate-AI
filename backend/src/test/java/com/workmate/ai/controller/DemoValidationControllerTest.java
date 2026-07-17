package com.workmate.ai.controller;

import com.workmate.ai.common.CommonResult;
import com.workmate.ai.dto.DemoValidationDTO;
import com.workmate.ai.exception.GlobalExceptionHandler;
import jakarta.validation.Valid;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@Import({
        GlobalExceptionHandler.class,
        DemoValidationControllerTest.TestValidationController.class
})
class DemoValidationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnParamErrorWhenRequestBodyInvalid() throws Exception {
        mockMvc.perform(post("/api/demo/validation")
                        .contentType("application/json")
                        .content("{\"name\":\"\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(40001)))
                .andExpect(jsonPath("$.message", is("请求参数错误")))
                .andExpect(jsonPath("$.data", nullValue()));
    }

    @RestController
    public static class TestValidationController {

        @PostMapping("/api/demo/validation")
        public CommonResult<String> validate(@Valid @RequestBody DemoValidationDTO request) {
            return CommonResult.success(request.getName());
        }
    }
}