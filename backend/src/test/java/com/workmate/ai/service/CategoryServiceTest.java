package com.workmate.ai.service;

import com.workmate.ai.common.ErrorCode;
import com.workmate.ai.entity.KnowledgeCategory;
import com.workmate.ai.entity.SysUser;
import com.workmate.ai.exception.BusinessException;
import com.workmate.ai.mapper.KnowledgeCategoryMapper;
import com.workmate.ai.mapper.SysUserMapper;
import com.workmate.ai.service.impl.CategoryServiceImpl;
import com.workmate.ai.vo.CategoryVO;
import com.workmate.ai.dto.CategoryCreateDTO;
import com.workmate.ai.dto.CategoryUpdateDTO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.ArgumentCaptor;
import com.workmate.ai.dto.CategoryStatusDTO;
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

    @Test
    void shouldCreateCategoryWhenUserIsAdmin() {
        CategoryCreateDTO request = createRequest("财务制度", "报销、预算和付款流程", 5);
        when(sysUserMapper.selectById(2L)).thenReturn(user(2L, "ADMIN", 1));
        when(categoryMapper.selectCount(any())).thenReturn(0L);

        CategoryVO result = categoryService.createCategory(2L, request);

        assertThat(result.getName()).isEqualTo("财务制度");
        assertThat(result.getDescription()).isEqualTo("报销、预算和付款流程");
        assertThat(result.getSortOrder()).isEqualTo(5);
        assertThat(result.getStatus()).isEqualTo(1);

        ArgumentCaptor<KnowledgeCategory> captor = ArgumentCaptor.forClass(KnowledgeCategory.class);
        verify(categoryMapper).insert(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("财务制度");
        assertThat(captor.getValue().getStatus()).isEqualTo(1);
        assertThat(captor.getValue().getIsDeleted()).isEqualTo(0);
    }

    @Test
    void shouldForbidEmployeeToCreateCategory() {
        CategoryCreateDTO request = createRequest("财务制度", "报销、预算和付款流程", 5);
        when(sysUserMapper.selectById(1L)).thenReturn(user(1L, "EMPLOYEE", 1));

        assertThatThrownBy(() -> categoryService.createCategory(1L, request))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN));

        verify(categoryMapper, never()).insert(any(KnowledgeCategory.class));
    }

    @Test
    void shouldRejectDuplicateCategoryNameWhenCreatingCategory() {
        CategoryCreateDTO request = createRequest("财务制度", "报销、预算和付款流程", 5);
        when(sysUserMapper.selectById(2L)).thenReturn(user(2L, "ADMIN", 1));
        when(categoryMapper.selectCount(any())).thenReturn(1L);

        assertThatThrownBy(() -> categoryService.createCategory(2L, request))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.STATE_CONFLICT));

        verify(categoryMapper, never()).insert(any(KnowledgeCategory.class));
    }

    @Test
    void shouldUpdateCategoryWhenUserIsAdmin() {
        CategoryUpdateDTO request = updateRequest("研发规范", "研发团队内部规范", 3);
        when(sysUserMapper.selectById(2L)).thenReturn(user(2L, "ADMIN", 1));
        when(categoryMapper.selectById(3L)).thenReturn(category(3L, "研发规范", 3, 1));
        when(categoryMapper.selectCount(any())).thenReturn(0L);

        CategoryVO result = categoryService.updateCategory(2L, 3L, request);

        assertThat(result.getId()).isEqualTo(3L);
        assertThat(result.getName()).isEqualTo("研发规范");
        assertThat(result.getDescription()).isEqualTo("研发团队内部规范");
        assertThat(result.getSortOrder()).isEqualTo(3);

        verify(categoryMapper).update(any(KnowledgeCategory.class), any(LambdaUpdateWrapper.class));
    }

    @Test
    void shouldForbidEmployeeToUpdateCategory() {
        CategoryUpdateDTO request = updateRequest("研发规范", "研发团队内部规范", 3);
        when(sysUserMapper.selectById(1L)).thenReturn(user(1L, "EMPLOYEE", 1));

        assertThatThrownBy(() -> categoryService.updateCategory(1L, 3L, request))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN));

        verify(categoryMapper, never()).update(any(KnowledgeCategory.class), any(LambdaUpdateWrapper.class));
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingMissingCategory() {
        CategoryUpdateDTO request = updateRequest("研发规范", "研发团队内部规范", 3);
        when(sysUserMapper.selectById(2L)).thenReturn(user(2L, "ADMIN", 1));
        when(categoryMapper.selectById(999L)).thenReturn(null);

        assertThatThrownBy(() -> categoryService.updateCategory(2L, 999L, request))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.DATA_NOT_FOUND));

        verify(categoryMapper, never()).update(any(KnowledgeCategory.class), any(LambdaUpdateWrapper.class));
    }

    @Test
    void shouldRejectDuplicateCategoryNameWhenUpdatingCategory() {
        CategoryUpdateDTO request = updateRequest("IT支持", "重复名称", 3);
        when(sysUserMapper.selectById(2L)).thenReturn(user(2L, "ADMIN", 1));
        when(categoryMapper.selectById(3L)).thenReturn(category(3L, "研发规范", 3, 1));
        when(categoryMapper.selectCount(any())).thenReturn(1L);

        assertThatThrownBy(() -> categoryService.updateCategory(2L, 3L, request))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.STATE_CONFLICT));

        verify(categoryMapper, never()).update(any(KnowledgeCategory.class), any(LambdaUpdateWrapper.class));
    }

    @Test
    void shouldUpdateCategoryStatusWhenUserIsAdmin() {
        CategoryStatusDTO request = statusRequest(0);
        when(sysUserMapper.selectById(2L)).thenReturn(user(2L, "ADMIN", 1));
        when(categoryMapper.selectById(3L)).thenReturn(category(3L, "研发规范", 3, 1));

        CategoryVO result = categoryService.updateCategoryStatus(2L, 3L, request);

        assertThat(result.getId()).isEqualTo(3L);
        assertThat(result.getStatus()).isEqualTo(0);
        verify(categoryMapper).update(any(KnowledgeCategory.class), any(LambdaUpdateWrapper.class));
    }

    @Test
    void shouldForbidEmployeeToUpdateCategoryStatus() {
        CategoryStatusDTO request = statusRequest(0);
        when(sysUserMapper.selectById(1L)).thenReturn(user(1L, "EMPLOYEE", 1));

        assertThatThrownBy(() -> categoryService.updateCategoryStatus(1L, 3L, request))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN));

        verify(categoryMapper, never()).update(any(KnowledgeCategory.class), any(LambdaUpdateWrapper.class));
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingMissingCategoryStatus() {
        CategoryStatusDTO request = statusRequest(0);
        when(sysUserMapper.selectById(2L)).thenReturn(user(2L, "ADMIN", 1));
        when(categoryMapper.selectById(999L)).thenReturn(null);

        assertThatThrownBy(() -> categoryService.updateCategoryStatus(2L, 999L, request))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.DATA_NOT_FOUND));

        verify(categoryMapper, never()).update(any(KnowledgeCategory.class), any(LambdaUpdateWrapper.class));
    }

    @Test
    void shouldDeleteCategoryWhenUserIsAdmin() {
        when(sysUserMapper.selectById(2L)).thenReturn(user(2L, "ADMIN", 1));
        when(categoryMapper.selectById(3L)).thenReturn(category(3L, "研发规范", 3, 1));

        Boolean result = categoryService.deleteCategory(2L, 3L);

        assertThat(result).isTrue();
        verify(categoryMapper).update(any(KnowledgeCategory.class), any(LambdaUpdateWrapper.class));
    }

    @Test
    void shouldForbidEmployeeToDeleteCategory() {
        when(sysUserMapper.selectById(1L)).thenReturn(user(1L, "EMPLOYEE", 1));

        assertThatThrownBy(() -> categoryService.deleteCategory(1L, 3L))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN));

        verify(categoryMapper, never()).update(any(KnowledgeCategory.class), any(LambdaUpdateWrapper.class));
    }

    @Test
    void shouldReturnNotFoundWhenDeletingMissingCategory() {
        when(sysUserMapper.selectById(2L)).thenReturn(user(2L, "ADMIN", 1));
        when(categoryMapper.selectById(999L)).thenReturn(null);

        assertThatThrownBy(() -> categoryService.deleteCategory(2L, 999L))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.DATA_NOT_FOUND));

        verify(categoryMapper, never()).update(any(KnowledgeCategory.class), any(LambdaUpdateWrapper.class));
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

    private CategoryCreateDTO createRequest(String name, String description, Integer sortOrder) {
        CategoryCreateDTO request = new CategoryCreateDTO();
        request.setName(name);
        request.setDescription(description);
        request.setSortOrder(sortOrder);
        return request;
    }

    private CategoryUpdateDTO updateRequest(String name, String description, Integer sortOrder) {
        CategoryUpdateDTO request = new CategoryUpdateDTO();
        request.setName(name);
        request.setDescription(description);
        request.setSortOrder(sortOrder);
        return request;
    }

    private CategoryStatusDTO statusRequest(Integer status) {
        CategoryStatusDTO request = new CategoryStatusDTO();
        request.setStatus(status);
        return request;
    }

}