package com.workmate.ai.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import com.workmate.ai.exception.GlobalExceptionHandler;
import org.springframework.context.annotation.Import;

import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(GlobalExceptionHandler.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnCurrentEmployeeWhenUserIdIsOne() throws Exception {
        mockMvc.perform(get("/api/users/current")
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(200)))
                .andExpect(jsonPath("$.message", is("success")))
                .andExpect(jsonPath("$.data.id", is(1)))
                .andExpect(jsonPath("$.data.username", is("employee_demo")))
                .andExpect(jsonPath("$.data.displayName", is("演示员工")))
                .andExpect(jsonPath("$.data.role", is("EMPLOYEE")))
                .andExpect(jsonPath("$.data.status", is(1)));
    }

    @Test
    void shouldReturnMissingUserWhenUserIdHeaderMissing() throws Exception {
        mockMvc.perform(get("/api/users/current"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(40002)))
                .andExpect(jsonPath("$.message", is("缺少用户身份")))
                .andExpect(jsonPath("$.data", nullValue()));
    }
}