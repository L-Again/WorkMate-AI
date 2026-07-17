package com.workmate.ai.controller;

import com.workmate.ai.exception.BusinessException;
import com.workmate.ai.exception.GlobalExceptionHandler;
import com.workmate.ai.common.ErrorCode;
import com.workmate.ai.service.CategoryService;
import com.workmate.ai.vo.CategoryVO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoryController.class)
@Import(GlobalExceptionHandler.class)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CategoryService categoryService;

    @Test
    void shouldListEnabledCategories() throws Exception {
        when(categoryService.listCategories(1L, false))
                .thenReturn(List.of(
                        new CategoryVO(1L, "人事制度", "请假、报销、考勤和转正等制度", 1, 1),
                        new CategoryVO(2L, "IT支持", "账号、网络、VPN和权限申请", 2, 1)
                ));

        mockMvc.perform(get("/api/knowledge/categories")
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(200)))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].name", is("人事制度")))
                .andExpect(jsonPath("$.data[1].name", is("IT支持")));
    }

    @Test
    void shouldReturnForbiddenWhenEmployeeIncludesDisabledCategories() throws Exception {
        when(categoryService.listCategories(1L, true))
                .thenThrow(new BusinessException(ErrorCode.FORBIDDEN));

        mockMvc.perform(get("/api/knowledge/categories")
                        .param("includeDisabled", "true")
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(40301)))
                .andExpect(jsonPath("$.message", is("无权限")));
    }
}