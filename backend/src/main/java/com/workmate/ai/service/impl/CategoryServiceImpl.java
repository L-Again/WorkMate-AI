package com.workmate.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.workmate.ai.common.ErrorCode;
import com.workmate.ai.entity.KnowledgeCategory;
import com.workmate.ai.entity.SysUser;
import com.workmate.ai.exception.BusinessException;
import com.workmate.ai.mapper.KnowledgeCategoryMapper;
import com.workmate.ai.mapper.SysUserMapper;
import com.workmate.ai.service.CategoryService;
import com.workmate.ai.vo.CategoryVO;
import com.workmate.ai.dto.CategoryCreateDTO;
import org.springframework.stereotype.Service;
import com.workmate.ai.dto.CategoryUpdateDTO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import java.util.List;
import com.workmate.ai.dto.CategoryStatusDTO;
import com.workmate.ai.service.AgentAnswerCacheService;

@Service
public class CategoryServiceImpl implements CategoryService {

    private static final int ENABLED_STATUS = 1;
    private static final int NOT_DELETED = 0;
    private static final String ADMIN_ROLE = "ADMIN";

    private final SysUserMapper sysUserMapper;
    private final KnowledgeCategoryMapper categoryMapper;
    private final AgentAnswerCacheService agentAnswerCacheService;

    public CategoryServiceImpl(SysUserMapper sysUserMapper,
                               KnowledgeCategoryMapper categoryMapper,
                               AgentAnswerCacheService agentAnswerCacheService) {
        this.sysUserMapper = sysUserMapper;
        this.categoryMapper = categoryMapper;
        this.agentAnswerCacheService = agentAnswerCacheService;
    }

    @Override
    public List<CategoryVO> listCategories(Long userId, Boolean includeDisabled) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null || !Integer.valueOf(ENABLED_STATUS).equals(user.getStatus())) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND_OR_DISABLED);
        }

        boolean includeAllStatuses = Boolean.TRUE.equals(includeDisabled);
        if (includeAllStatuses && !ADMIN_ROLE.equals(user.getRole())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        LambdaQueryWrapper<KnowledgeCategory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(KnowledgeCategory::getIsDeleted, NOT_DELETED);
        if (!includeAllStatuses) {
            queryWrapper.eq(KnowledgeCategory::getStatus, ENABLED_STATUS);
        }
        queryWrapper.orderByAsc(KnowledgeCategory::getSortOrder, KnowledgeCategory::getId);

        return categoryMapper.selectList(queryWrapper)
                .stream()
                .map(this::toVO)
                .toList();
    }

    private CategoryVO toVO(KnowledgeCategory category) {
        return new CategoryVO(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getSortOrder(),
                category.getStatus()
        );
    }

    @Override
    public CategoryVO getCategoryDetail(Long userId, Long categoryId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null || !Integer.valueOf(ENABLED_STATUS).equals(user.getStatus())) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND_OR_DISABLED);
        }

        KnowledgeCategory category = categoryMapper.selectById(categoryId);
        if (category == null || !Integer.valueOf(NOT_DELETED).equals(category.getIsDeleted())) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND);
        }

        boolean isAdmin = ADMIN_ROLE.equals(user.getRole());
        if (!isAdmin && !Integer.valueOf(ENABLED_STATUS).equals(category.getStatus())) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND);
        }

        return toVO(category);
    }

    @Override
    public CategoryVO createCategory(Long userId, CategoryCreateDTO request) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null || !Integer.valueOf(ENABLED_STATUS).equals(user.getStatus())) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND_OR_DISABLED);
        }

        if (!ADMIN_ROLE.equals(user.getRole())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        LambdaQueryWrapper<KnowledgeCategory> duplicateQuery = new LambdaQueryWrapper<>();
        duplicateQuery.eq(KnowledgeCategory::getName, request.getName());
        duplicateQuery.eq(KnowledgeCategory::getIsDeleted, NOT_DELETED);
        if (categoryMapper.selectCount(duplicateQuery) > 0) {
            throw new BusinessException(ErrorCode.STATE_CONFLICT);
        }

        KnowledgeCategory category = new KnowledgeCategory();
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setSortOrder(request.getSortOrder());
        category.setStatus(ENABLED_STATUS);
        category.setIsDeleted(NOT_DELETED);

        categoryMapper.insert(category);
        agentAnswerCacheService.evictAllAnswers();

        return toVO(category);
    }

    @Override
    public CategoryVO updateCategory(Long userId, Long categoryId, CategoryUpdateDTO request) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null || !Integer.valueOf(ENABLED_STATUS).equals(user.getStatus())) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND_OR_DISABLED);
        }

        if (!ADMIN_ROLE.equals(user.getRole())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        KnowledgeCategory existingCategory = categoryMapper.selectById(categoryId);
        if (existingCategory == null || !Integer.valueOf(NOT_DELETED).equals(existingCategory.getIsDeleted())) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND);
        }

        LambdaQueryWrapper<KnowledgeCategory> duplicateQuery = new LambdaQueryWrapper<>();
        duplicateQuery.eq(KnowledgeCategory::getName, request.getName());
        duplicateQuery.eq(KnowledgeCategory::getIsDeleted, NOT_DELETED);
        duplicateQuery.ne(KnowledgeCategory::getId, categoryId);
        if (categoryMapper.selectCount(duplicateQuery) > 0) {
            throw new BusinessException(ErrorCode.STATE_CONFLICT);
        }

        KnowledgeCategory updatedCategory = new KnowledgeCategory();
        updatedCategory.setName(request.getName());
        updatedCategory.setDescription(request.getDescription());
        updatedCategory.setSortOrder(request.getSortOrder());

        LambdaUpdateWrapper<KnowledgeCategory> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(KnowledgeCategory::getId, categoryId);
        updateWrapper.eq(KnowledgeCategory::getIsDeleted, NOT_DELETED);

        categoryMapper.update(updatedCategory, updateWrapper);
        agentAnswerCacheService.evictAllAnswers();

        existingCategory.setName(request.getName());
        existingCategory.setDescription(request.getDescription());
        existingCategory.setSortOrder(request.getSortOrder());

        return toVO(existingCategory);
    }

    @Override
    public CategoryVO updateCategoryStatus(Long userId, Long categoryId, CategoryStatusDTO request) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null || !Integer.valueOf(ENABLED_STATUS).equals(user.getStatus())) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND_OR_DISABLED);
        }

        if (!ADMIN_ROLE.equals(user.getRole())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        KnowledgeCategory existingCategory = categoryMapper.selectById(categoryId);
        if (existingCategory == null || !Integer.valueOf(NOT_DELETED).equals(existingCategory.getIsDeleted())) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND);
        }

        KnowledgeCategory updatedCategory = new KnowledgeCategory();
        updatedCategory.setStatus(request.getStatus());

        LambdaUpdateWrapper<KnowledgeCategory> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(KnowledgeCategory::getId, categoryId);
        updateWrapper.eq(KnowledgeCategory::getIsDeleted, NOT_DELETED);

        categoryMapper.update(updatedCategory, updateWrapper);
        agentAnswerCacheService.evictAllAnswers();

        existingCategory.setStatus(request.getStatus());
        return toVO(existingCategory);
    }

    @Override
    public Boolean deleteCategory(Long userId, Long categoryId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null || !Integer.valueOf(ENABLED_STATUS).equals(user.getStatus())) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND_OR_DISABLED);
        }

        if (!ADMIN_ROLE.equals(user.getRole())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        KnowledgeCategory existingCategory = categoryMapper.selectById(categoryId);
        if (existingCategory == null || !Integer.valueOf(NOT_DELETED).equals(existingCategory.getIsDeleted())) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND);
        }

        KnowledgeCategory deletedCategory = new KnowledgeCategory();
        deletedCategory.setIsDeleted(1);

        LambdaUpdateWrapper<KnowledgeCategory> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(KnowledgeCategory::getId, categoryId);
        updateWrapper.eq(KnowledgeCategory::getIsDeleted, NOT_DELETED);

        categoryMapper.update(deletedCategory, updateWrapper);
        agentAnswerCacheService.evictAllAnswers();

        return true;
    }
}