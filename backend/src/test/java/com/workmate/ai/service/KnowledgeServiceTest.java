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
import com.workmate.ai.dto.KnowledgeUpdateDTO;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;

import com.workmate.ai.dto.KnowledgeCreateDTO;
import com.workmate.ai.entity.Knowledge;
import com.workmate.ai.entity.KnowledgeCategory;
import com.workmate.ai.mapper.KnowledgeCategoryMapper;
import org.mockito.ArgumentCaptor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class KnowledgeServiceTest {

    @Mock
    private SysUserMapper sysUserMapper;

    @Mock
    private KnowledgeMapper knowledgeMapper;

    private KnowledgeService knowledgeService;

    @Mock
    private KnowledgeCategoryMapper categoryMapper;

    @BeforeEach
    void setUp() {
        knowledgeService = new KnowledgeServiceImpl(sysUserMapper, knowledgeMapper, categoryMapper);
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

    @Test
    void shouldCreateKnowledgeWhenAdminAndCategoryExists() {
        KnowledgeCreateDTO request = new KnowledgeCreateDTO();
        request.setCategoryId(3L);
        request.setTitle("Java 接口返回规范");
        request.setKeywords("Java,接口,返回");
        request.setContent("后端接口统一使用 CommonResult 返回。");
        request.setStatus(1);

        KnowledgeCategory category = new KnowledgeCategory();
        category.setId(3L);
        category.setName("研发规范");
        category.setStatus(1);
        category.setIsDeleted(0);

        when(sysUserMapper.selectById(2L)).thenReturn(user(2L, "ADMIN", 1));
        when(categoryMapper.selectById(3L)).thenReturn(category);
        when(knowledgeMapper.insert(any(Knowledge.class))).thenAnswer(invocation -> {
            Knowledge inserted = invocation.getArgument(0);
            inserted.setId(10L);
            return 1;
        });
        when(knowledgeMapper.selectKnowledgeDetail(10L))
                .thenReturn(new KnowledgeDetailVO(
                        10L,
                        3L,
                        "研发规范",
                        "Java 接口返回规范",
                        "Java,接口,返回",
                        "后端接口统一使用 CommonResult 返回。",
                        1,
                        LocalDateTime.of(2026, 7, 18, 17, 0)
                ));

        KnowledgeDetailVO result = knowledgeService.createKnowledge(2L, request);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getTitle()).isEqualTo("Java 接口返回规范");

        ArgumentCaptor<Knowledge> captor = ArgumentCaptor.forClass(Knowledge.class);
        verify(knowledgeMapper).insert(captor.capture());

        Knowledge inserted = captor.getValue();
        assertThat(inserted.getCategoryId()).isEqualTo(3L);
        assertThat(inserted.getTitle()).isEqualTo("Java 接口返回规范");
        assertThat(inserted.getKeywords()).isEqualTo("Java,接口,返回");
        assertThat(inserted.getContent()).isEqualTo("后端接口统一使用 CommonResult 返回。");
        assertThat(inserted.getStatus()).isEqualTo(1);
        assertThat(inserted.getIsDeleted()).isEqualTo(0);
        assertThat(inserted.getCreatedBy()).isEqualTo(2L);
        assertThat(inserted.getUpdatedBy()).isEqualTo(2L);
    }

    @Test
    void shouldReturnForbiddenWhenEmployeeCreatesKnowledge() {
        KnowledgeCreateDTO request = new KnowledgeCreateDTO();
        request.setCategoryId(3L);
        request.setTitle("员工新增知识");
        request.setKeywords("员工,新增");
        request.setContent("员工不允许新增知识。");
        request.setStatus(1);

        when(sysUserMapper.selectById(1L)).thenReturn(user(1L, "EMPLOYEE", 1));

        assertThatThrownBy(() -> knowledgeService.createKnowledge(1L, request))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN));

        verify(knowledgeMapper, never()).insert(any(Knowledge.class));
    }

    @Test
    void shouldReturnNotFoundWhenCreateKnowledgeCategoryDoesNotExist() {
        KnowledgeCreateDTO request = new KnowledgeCreateDTO();
        request.setCategoryId(999L);
        request.setTitle("不存在分类知识");
        request.setKeywords("分类,不存在");
        request.setContent("分类不存在时不能新增知识。");
        request.setStatus(1);

        when(sysUserMapper.selectById(2L)).thenReturn(user(2L, "ADMIN", 1));
        when(categoryMapper.selectById(999L)).thenReturn(null);

        assertThatThrownBy(() -> knowledgeService.createKnowledge(2L, request))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.DATA_NOT_FOUND));

        verify(knowledgeMapper, never()).insert(any(Knowledge.class));
    }

    @Test
    void shouldUpdateKnowledgeWhenAdminAndCategoryExists() {
        KnowledgeUpdateDTO request = new KnowledgeUpdateDTO();
        request.setCategoryId(3L);
        request.setTitle("Git 分支命名规范更新");
        request.setKeywords("Git,分支,branch,规范");
        request.setContent("功能分支统一使用 feature/功能名称，修复分支统一使用 bugfix/问题名称。");
        request.setStatus(1);

        Knowledge existing = new Knowledge();
        existing.setId(1L);
        existing.setCategoryId(3L);
        existing.setTitle("Git 分支命名规范");
        existing.setKeywords("Git,分支,branch");
        existing.setContent("功能分支统一使用 feature/功能名称。");
        existing.setStatus(1);
        existing.setIsDeleted(0);

        KnowledgeCategory category = new KnowledgeCategory();
        category.setId(3L);
        category.setName("研发规范");
        category.setStatus(1);
        category.setIsDeleted(0);

        when(sysUserMapper.selectById(2L)).thenReturn(user(2L, "ADMIN", 1));
        when(knowledgeMapper.selectById(1L)).thenReturn(existing);
        when(categoryMapper.selectById(3L)).thenReturn(category);
        when(knowledgeMapper.updateById(any(Knowledge.class))).thenReturn(1);
        when(knowledgeMapper.selectKnowledgeDetail(1L))
                .thenReturn(new KnowledgeDetailVO(
                        1L,
                        3L,
                        "研发规范",
                        "Git 分支命名规范更新",
                        "Git,分支,branch,规范",
                        "功能分支统一使用 feature/功能名称，修复分支统一使用 bugfix/问题名称。",
                        1,
                        LocalDateTime.of(2026, 7, 18, 17, 40)
                ));

        KnowledgeDetailVO result = knowledgeService.updateKnowledge(2L, 1L, request);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Git 分支命名规范更新");

        ArgumentCaptor<Knowledge> captor = ArgumentCaptor.forClass(Knowledge.class);
        verify(knowledgeMapper).updateById(captor.capture());

        Knowledge updated = captor.getValue();
        assertThat(updated.getId()).isEqualTo(1L);
        assertThat(updated.getCategoryId()).isEqualTo(3L);
        assertThat(updated.getTitle()).isEqualTo("Git 分支命名规范更新");
        assertThat(updated.getKeywords()).isEqualTo("Git,分支,branch,规范");
        assertThat(updated.getContent()).isEqualTo("功能分支统一使用 feature/功能名称，修复分支统一使用 bugfix/问题名称。");
        assertThat(updated.getStatus()).isEqualTo(1);
        assertThat(updated.getUpdatedBy()).isEqualTo(2L);
    }

    @Test
    void shouldReturnForbiddenWhenEmployeeUpdatesKnowledge() {
        KnowledgeUpdateDTO request = new KnowledgeUpdateDTO();
        request.setCategoryId(3L);
        request.setTitle("员工修改知识");
        request.setKeywords("员工,修改");
        request.setContent("员工不允许修改知识。");
        request.setStatus(1);

        when(sysUserMapper.selectById(1L)).thenReturn(user(1L, "EMPLOYEE", 1));

        assertThatThrownBy(() -> knowledgeService.updateKnowledge(1L, 1L, request))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN));

        verify(knowledgeMapper, never()).updateById(any(Knowledge.class));
    }

    @Test
    void shouldReturnNotFoundWhenUpdateKnowledgeDoesNotExist() {
        KnowledgeUpdateDTO request = new KnowledgeUpdateDTO();
        request.setCategoryId(3L);
        request.setTitle("不存在知识");
        request.setKeywords("知识,不存在");
        request.setContent("知识不存在时不能修改。");
        request.setStatus(1);

        when(sysUserMapper.selectById(2L)).thenReturn(user(2L, "ADMIN", 1));
        when(knowledgeMapper.selectById(999L)).thenReturn(null);

        assertThatThrownBy(() -> knowledgeService.updateKnowledge(2L, 999L, request))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.DATA_NOT_FOUND));

        verify(knowledgeMapper, never()).updateById(any(Knowledge.class));
    }

    @Test
    void shouldReturnNotFoundWhenUpdateKnowledgeIsDeleted() {
        KnowledgeUpdateDTO request = new KnowledgeUpdateDTO();
        request.setCategoryId(3L);
        request.setTitle("已删除知识");
        request.setKeywords("知识,删除");
        request.setContent("已删除知识不能修改。");
        request.setStatus(1);

        Knowledge deleted = new Knowledge();
        deleted.setId(1L);
        deleted.setIsDeleted(1);

        when(sysUserMapper.selectById(2L)).thenReturn(user(2L, "ADMIN", 1));
        when(knowledgeMapper.selectById(1L)).thenReturn(deleted);

        assertThatThrownBy(() -> knowledgeService.updateKnowledge(2L, 1L, request))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.DATA_NOT_FOUND));

        verify(knowledgeMapper, never()).updateById(any(Knowledge.class));
    }

    @Test
    void shouldReturnNotFoundWhenUpdateKnowledgeCategoryDoesNotExist() {
        KnowledgeUpdateDTO request = new KnowledgeUpdateDTO();
        request.setCategoryId(999L);
        request.setTitle("分类不存在知识");
        request.setKeywords("分类,不存在");
        request.setContent("分类不存在时不能修改知识。");
        request.setStatus(1);

        Knowledge existing = new Knowledge();
        existing.setId(1L);
        existing.setIsDeleted(0);

        when(sysUserMapper.selectById(2L)).thenReturn(user(2L, "ADMIN", 1));
        when(knowledgeMapper.selectById(1L)).thenReturn(existing);
        when(categoryMapper.selectById(999L)).thenReturn(null);

        assertThatThrownBy(() -> knowledgeService.updateKnowledge(2L, 1L, request))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.DATA_NOT_FOUND));

        verify(knowledgeMapper, never()).updateById(any(Knowledge.class));
    }

    @Test
    void shouldReturnNotFoundWhenUpdateKnowledgeCategoryIsDeleted() {
        KnowledgeUpdateDTO request = new KnowledgeUpdateDTO();
        request.setCategoryId(3L);
        request.setTitle("分类已删除知识");
        request.setKeywords("分类,删除");
        request.setContent("分类已删除时不能修改知识。");
        request.setStatus(1);

        Knowledge existing = new Knowledge();
        existing.setId(1L);
        existing.setIsDeleted(0);

        KnowledgeCategory deletedCategory = new KnowledgeCategory();
        deletedCategory.setId(3L);
        deletedCategory.setIsDeleted(1);

        when(sysUserMapper.selectById(2L)).thenReturn(user(2L, "ADMIN", 1));
        when(knowledgeMapper.selectById(1L)).thenReturn(existing);
        when(categoryMapper.selectById(3L)).thenReturn(deletedCategory);

        assertThatThrownBy(() -> knowledgeService.updateKnowledge(2L, 1L, request))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.DATA_NOT_FOUND));

        verify(knowledgeMapper, never()).updateById(any(Knowledge.class));
    }
}