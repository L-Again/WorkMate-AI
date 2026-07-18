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
import com.workmate.ai.dto.CategoryCreateDTO;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@WebMvcTest(CategoryController.class)
@Import(GlobalExceptionHandler.class)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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

    @Test
    void shouldGetCategoryDetail() throws Exception {
        when(categoryService.getCategoryDetail(1L, 1L))
                .thenReturn(new CategoryVO(1L, "人事制度", "请假、报销、考勤和转正等制度", 1, 1));

        mockMvc.perform(get("/api/knowledge/categories/{id}", 1L)
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(200)))
                .andExpect(jsonPath("$.data.id", is(1)))
                .andExpect(jsonPath("$.data.name", is("人事制度")))
                .andExpect(jsonPath("$.data.status", is(1)));

        verify(categoryService).getCategoryDetail(1L, 1L);
    }

    @Test
    void shouldReturnNotFoundWhenCategoryDoesNotExist() throws Exception {
        when(categoryService.getCategoryDetail(1L, 999L))
                .thenThrow(new BusinessException(ErrorCode.DATA_NOT_FOUND));

        mockMvc.perform(get("/api/knowledge/categories/{id}", 999L)
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(40401)))
                .andExpect(jsonPath("$.message", is("数据不存在")));
    }

    @Test
    void shouldCreateCategoryWhenUserIsAdmin() throws Exception {
        CategoryCreateDTO request = new CategoryCreateDTO();
        request.setName("财务制度");
        request.setDescription("报销、预算和付款流程");
        request.setSortOrder(5);

        when(categoryService.createCategory(eq(2L), any(CategoryCreateDTO.class)))
                .thenReturn(new CategoryVO(5L, "财务制度", "报销、预算和付款流程", 5, 1));

        mockMvc.perform(post("/api/knowledge/categories")
                        .header("X-User-Id", "2")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(200)))
                .andExpect(jsonPath("$.data.id", is(5)))
                .andExpect(jsonPath("$.data.name", is("财务制度")))
                .andExpect(jsonPath("$.data.status", is(1)));
    }

}