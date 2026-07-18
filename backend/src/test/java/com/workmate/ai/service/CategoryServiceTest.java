package com.workmate.ai.service;

import com.workmate.ai.common.ErrorCode;
import com.workmate.ai.entity.KnowledgeCategory;
import com.workmate.ai.entity.SysUser;
import com.workmate.ai.exception.BusinessException;
import com.workmate.ai.mapper.KnowledgeCategoryMapper;
import com.workmate.ai.mapper.SysUserMapper;
import com.workmate.ai.service.impl.CategoryServiceImpl;
import com.workmate.ai.vo.CategoryVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private SysUserMapper sysUserMapper;

    @Mock
    private KnowledgeCategoryMapper categoryMapper;

    private CategoryService categoryService;

    @BeforeEach
    void setUp() {
        categoryService = new CategoryServiceImpl(sysUserMapper, categoryMapper);
    }

    @Test
    void shouldListOnlyEnabledAndNotDeletedCategoriesByDefault() {
        when(sysUserMapper.selectById(1L)).thenReturn(user(1L, "EMPLOYEE", 1));
        when(categoryMapper.selectList(any())).thenReturn(List.of(category(1L, "人事制度", 1, 1)));

        List<CategoryVO> result = categoryService.listCategories(1L, false);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("人事制度");
        verify(categoryMapper).selectList(any());
    }

    @Test
    void shouldAllowAdminToIncludeDisabledCategories() {
        when(sysUserMapper.selectById(2L)).thenReturn(user(2L, "ADMIN", 1));
        when(categoryMapper.selectList(any())).thenReturn(List.of(
                category(1L, "人事制度", 1, 1),
                category(2L, "停用分类", 2, 0)
        ));

        List<CategoryVO> result = categoryService.listCategories(2L, true);

        assertThat(result).extracting(CategoryVO::getStatus).containsExactly(1, 0);
        verify(categoryMapper).selectList(any());
    }

    @Test
    void shouldForbidEmployeeToIncludeDisabledCategories() {
        when(sysUserMapper.selectById(1L)).thenReturn(user(1L, "EMPLOYEE", 1));

        assertThatThrownBy(() -> categoryService.listCategories(1L, true))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN));

        verify(categoryMapper, never()).selectList(any());
    }

    @Test
    void shouldGetEnabledCategoryDetailForEmployee() {
        when(sysUserMapper.selectById(1L)).thenReturn(user(1L, "EMPLOYEE", 1));
        when(categoryMapper.selectById(1L)).thenReturn(category(1L, "人事制度", 1, 1));

        CategoryVO result = categoryService.getCategoryDetail(1L, 1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("人事制度");
        assertThat(result.getStatus()).isEqualTo(1);
    }

    @Test
    void shouldGetDisabledCategoryDetailForAdmin() {
        when(sysUserMapper.selectById(2L)).thenReturn(user(2L, "ADMIN", 1));
        when(categoryMapper.selectById(4L)).thenReturn(category(4L, "项目流程", 4, 0));

        CategoryVO result = categoryService.getCategoryDetail(2L, 4L);

        assertThat(result.getId()).isEqualTo(4L);
        assertThat(result.getName()).isEqualTo("项目流程");
        assertThat(result.getStatus()).isEqualTo(0);
    }

    @Test
    void shouldReturnNotFoundWhenEmployeeGetsDisabledCategoryDetail() {
        when(sysUserMapper.selectById(1L)).thenReturn(user(1L, "EMPLOYEE", 1));
        when(categoryMapper.selectById(4L)).thenReturn(category(4L, "项目流程", 4, 0));

        assertThatThrownBy(() -> categoryService.getCategoryDetail(1L, 4L))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.DATA_NOT_FOUND));
    }

    @Test
    void shouldReturnNotFoundWhenCategoryDetailDoesNotExist() {
        when(sysUserMapper.selectById(1L)).thenReturn(user(1L, "EMPLOYEE", 1));
        when(categoryMapper.selectById(999L)).thenReturn(null);

        assertThatThrownBy(() -> categoryService.getCategoryDetail(1L, 999L))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.DATA_NOT_FOUND));
    }

    private SysUser user(Long id, String role, Integer status) {
        SysUser user = new SysUser();
        user.setId(id);
        user.setRole(role);
        user.setStatus(status);
        return user;
    }

    private KnowledgeCategory category(Long id, String name, Integer sortOrder, Integer status) {
        KnowledgeCategory category = new KnowledgeCategory();
        category.setId(id);
        category.setName(name);
        category.setDescription(name + "说明");
        category.setSortOrder(sortOrder);
        category.setStatus(status);
        category.setIsDeleted(0);
        return category;
    }

}