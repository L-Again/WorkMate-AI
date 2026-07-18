package com.workmate.ai.service;

import com.workmate.ai.common.ErrorCode;
import com.workmate.ai.common.PageResult;
import com.workmate.ai.entity.SysUser;
import com.workmate.ai.exception.BusinessException;
import com.workmate.ai.mapper.KnowledgeMapper;
import com.workmate.ai.mapper.SysUserMapper;
import com.workmate.ai.service.impl.KnowledgeServiceImpl;
import com.workmate.ai.vo.KnowledgeListItemVO;
import com.workmate.ai.vo.KnowledgeDetailVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KnowledgeServiceTest {

    @Mock
    private SysUserMapper sysUserMapper;

    @Mock
    private KnowledgeMapper knowledgeMapper;

    private KnowledgeService knowledgeService;

    @BeforeEach
    void setUp() {
        knowledgeService = new KnowledgeServiceImpl(sysUserMapper, knowledgeMapper);
    }

    @Test
    void shouldPageKnowledge() {
        when(sysUserMapper.selectById(1L)).thenReturn(user(1L, "EMPLOYEE", 1));
        when(knowledgeMapper.countKnowledgePage("Git", 3L, 1)).thenReturn(1L);
        when(knowledgeMapper.selectKnowledgePage(0L, 10L, "Git", 3L, 1))
                .thenReturn(List.of(new KnowledgeListItemVO(
                        1L,
                        3L,
                        "研发规范",
                        "Git 分支命名规范",
                        "Git,分支,branch",
                        1,
                        LocalDateTime.of(2026, 7, 16, 19, 0)
                )));

        PageResult<KnowledgeListItemVO> result = knowledgeService.pageKnowledge(1L, 1L, 10L, "Git", 3L, 1);

        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getRecords().get(0).getTitle()).isEqualTo("Git 分支命名规范");
        assertThat(result.getPageNum()).isEqualTo(1L);
        assertThat(result.getPageSize()).isEqualTo(10L);
        assertThat(result.getTotal()).isEqualTo(1L);
        assertThat(result.getPages()).isEqualTo(1L);
    }

    @Test
    void shouldThrowWhenUserDoesNotExist() {
        when(sysUserMapper.selectById(999L)).thenReturn(null);

        assertThatThrownBy(() -> knowledgeService.pageKnowledge(999L, 1L, 10L, null, null, null))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND_OR_DISABLED));
    }

    private SysUser user(Long id, String role, Integer status) {
        SysUser user = new SysUser();
        user.setId(id);
        user.setRole(role);
        user.setStatus(status);
        return user;
    }

    @Test
    void shouldGetKnowledgeDetail() {
        KnowledgeDetailVO detail = new KnowledgeDetailVO(
                1L,
                3L,
                "研发规范",
                "Git 分支命名规范",
                "Git,分支,branch",
                "功能分支统一使用 feature/功能名称。",
                1,
                LocalDateTime.of(2026, 7, 16, 19, 0)
        );
        when(sysUserMapper.selectById(1L)).thenReturn(user(1L, "EMPLOYEE", 1));
        when(knowledgeMapper.selectKnowledgeDetail(1L)).thenReturn(detail);

        KnowledgeDetailVO result = knowledgeService.getKnowledgeDetail(1L, 1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Git 分支命名规范");
        assertThat(result.getContent()).isEqualTo("功能分支统一使用 feature/功能名称。");
    }

    @Test
    void shouldReturnNotFoundWhenKnowledgeDetailDoesNotExist() {
        when(sysUserMapper.selectById(1L)).thenReturn(user(1L, "EMPLOYEE", 1));
        when(knowledgeMapper.selectKnowledgeDetail(999L)).thenReturn(null);

        assertThatThrownBy(() -> knowledgeService.getKnowledgeDetail(1L, 999L))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.DATA_NOT_FOUND));
    }
}