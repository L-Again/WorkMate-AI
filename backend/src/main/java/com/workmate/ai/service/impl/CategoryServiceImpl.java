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

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    private static final int ENABLED_STATUS = 1;
    private static final int NOT_DELETED = 0;
    private static final String ADMIN_ROLE = "ADMIN";

    private final SysUserMapper sysUserMapper;
    private final KnowledgeCategoryMapper categoryMapper;

    public CategoryServiceImpl(SysUserMapper sysUserMapper, KnowledgeCategoryMapper categoryMapper) {
        this.sysUserMapper = sysUserMapper;
        this.categoryMapper = categoryMapper;
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

        return toVO(category);
    }

}