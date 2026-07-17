package com.workmate.ai.controller;

import com.workmate.ai.common.CommonResult;
import com.workmate.ai.dto.DemoValidationDTO;
import com.workmate.ai.exception.GlobalExceptionHandler;
import jakarta.validation.Valid;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DemoValidationControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(new TestValidationController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

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
    static class TestValidationController {

        @PostMapping("/api/demo/validation")
        CommonResult<String> validate(@Valid @RequestBody DemoValidationDTO request) {
            return CommonResult.success(request.getName());
        }
    }
}