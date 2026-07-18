package com.workmate.ai.controller;

import com.workmate.ai.common.PageResult;
import com.workmate.ai.exception.GlobalExceptionHandler;
import com.workmate.ai.service.KnowledgeService;
import com.workmate.ai.vo.KnowledgeListItemVO;
import com.workmate.ai.exception.BusinessException;
import com.workmate.ai.common.ErrorCode;
import com.workmate.ai.vo.KnowledgeDetailVO;
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

@WebMvcTest(KnowledgeController.class)
@Import(GlobalExceptionHandler.class)
class KnowledgeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private KnowledgeService knowledgeService;

    @Test
    void shouldPageKnowledgeList() throws Exception {
        PageResult<KnowledgeListItemVO> pageResult = new PageResult<>(
                List.of(new KnowledgeListItemVO(
                        1L,
                        3L,
                        "研发规范",
                        "Git 分支命名规范",
                        "Git,分支,branch",
                        1,
                        LocalDateTime.of(2026, 7, 16, 19, 0)
                )),
                1L,
                10L,
                1L,
                1L
        );

        when(knowledgeService.pageKnowledge(1L, 1L, 10L, "Git", 3L, 1))
                .thenReturn(pageResult);

        mockMvc.perform(get("/api/knowledge")
                        .header("X-User-Id", "1")
                        .param("pageNum", "1")
                        .param("pageSize", "10")
                        .param("keyword", "Git")
                        .param("categoryId", "3")
                        .param("status", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(200)))
                .andExpect(jsonPath("$.data.records", hasSize(1)))
                .andExpect(jsonPath("$.data.records[0].title", is("Git 分支命名规范")))
                .andExpect(jsonPath("$.data.records[0].categoryName", is("研发规范")))
                .andExpect(jsonPath("$.data.pageNum", is(1)))
                .andExpect(jsonPath("$.data.total", is(1)));
    }

    @Test
    void shouldGetKnowledgeDetail() throws Exception {
        when(knowledgeService.getKnowledgeDetail(1L, 1L))
                .thenReturn(new KnowledgeDetailVO(
                        1L,
                        3L,
                        "研发规范",
                        "Git 分支命名规范",
                        "Git,分支,branch",
                        "功能分支统一使用 feature/功能名称。",
                        1,
                        LocalDateTime.of(2026, 7, 16, 19, 0)
                ));

        mockMvc.perform(get("/api/knowledge/{id}", 1L)
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(200)))
                .andExpect(jsonPath("$.data.id", is(1)))
                .andExpect(jsonPath("$.data.categoryName", is("研发规范")))
                .andExpect(jsonPath("$.data.title", is("Git 分支命名规范")))
                .andExpect(jsonPath("$.data.content", is("功能分支统一使用 feature/功能名称。")));
    }

    @Test
    void shouldReturnNotFoundWhenKnowledgeDetailDoesNotExist() throws Exception {
        when(knowledgeService.getKnowledgeDetail(1L, 999L))
                .thenThrow(new BusinessException(ErrorCode.DATA_NOT_FOUND));

        mockMvc.perform(get("/api/knowledge/{id}", 999L)
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(40401)))
                .andExpect(jsonPath("$.message", is("数据不存在")));
    }
}