package com.workmate.ai.tool;

import com.workmate.ai.common.ErrorCode;
import com.workmate.ai.exception.BusinessException;
import com.workmate.ai.service.KnowledgeService;
import com.workmate.ai.tool.impl.SearchKnowledgeTool;
import com.workmate.ai.vo.KnowledgeListItemVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchKnowledgeToolTest {

    @Mock
    private KnowledgeService knowledgeService;

    private SearchKnowledgeTool searchKnowledgeTool;

    @BeforeEach
    void setUp() {
        searchKnowledgeTool = new SearchKnowledgeTool(knowledgeService);
    }

    @Test
    void shouldExecuteSearchKnowledgeTool() {
        List<KnowledgeListItemVO> records = List.of(new KnowledgeListItemVO(
                3L,
                3L,
                "研发规范",
                "Git 分支命名规范",
                "Git,分支,branch",
                1,
                LocalDateTime.of(2026, 7, 19, 18, 0)
        ));

        when(knowledgeService.searchKnowledge(1L, "Git 分支", 3)).thenReturn(records);

        Object result = searchKnowledgeTool.execute(Map.of(
                "userId", 1L,
                "keyword", " Git 分支 ",
                "limit", 3
        ));

        assertThat(result).isEqualTo(records);
        verify(knowledgeService).searchKnowledge(1L, "Git 分支", 3);
    }

    @Test
    void shouldUseDefaultLimitWhenLimitMissing() {
        when(knowledgeService.searchKnowledge(1L, "Git", 5)).thenReturn(List.of());

        Object result = searchKnowledgeTool.execute(Map.of(
                "userId", 1L,
                "keyword", "Git"
        ));

        assertThat(result).isEqualTo(List.of());
        verify(knowledgeService).searchKnowledge(1L, "Git", 5);
    }

    @Test
    void shouldCapLimitAtFive() {
        when(knowledgeService.searchKnowledge(1L, "Git", 5)).thenReturn(List.of());

        searchKnowledgeTool.execute(Map.of(
                "userId", 1L,
                "keyword", "Git",
                "limit", 20
        ));

        verify(knowledgeService).searchKnowledge(1L, "Git", 5);
    }

    @Test
    void shouldRejectBlankKeyword() {
        assertThatThrownBy(() -> searchKnowledgeTool.execute(Map.of(
                "userId", 1L,
                "keyword", " "
        )))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.PARAM_ERROR));
    }
}