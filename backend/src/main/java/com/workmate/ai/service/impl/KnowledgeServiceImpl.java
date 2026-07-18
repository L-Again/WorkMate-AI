package com.workmate.ai.service.impl;

import com.workmate.ai.common.ErrorCode;
import com.workmate.ai.common.PageResult;
import com.workmate.ai.entity.SysUser;
import com.workmate.ai.exception.BusinessException;
import com.workmate.ai.mapper.KnowledgeMapper;
import com.workmate.ai.mapper.SysUserMapper;
import com.workmate.ai.service.KnowledgeService;
import com.workmate.ai.vo.KnowledgeListItemVO;
import com.workmate.ai.vo.KnowledgeDetailVO;
import com.workmate.ai.dto.KnowledgeCreateDTO;
import org.springframework.stereotype.Service;
import com.workmate.ai.entity.Knowledge;
import com.workmate.ai.entity.KnowledgeCategory;
import com.workmate.ai.mapper.KnowledgeCategoryMapper;
import com.workmate.ai.dto.KnowledgeUpdateDTO;
import com.workmate.ai.dto.KnowledgeStatusDTO;


import java.util.List;

@Service
public class KnowledgeServiceImpl implements KnowledgeService {

    private static final int ENABLED_STATUS = 1;
    private static final int NOT_DELETED = 0;
    private static final String ADMIN_ROLE = "ADMIN";

    private final SysUserMapper sysUserMapper;
    private final KnowledgeMapper knowledgeMapper;
    private final KnowledgeCategoryMapper categoryMapper;

    public KnowledgeServiceImpl(SysUserMapper sysUserMapper,
                                KnowledgeMapper knowledgeMapper,
                                KnowledgeCategoryMapper categoryMapper) {
        this.sysUserMapper = sysUserMapper;
        this.knowledgeMapper = knowledgeMapper;
        this.categoryMapper = categoryMapper;
    }

    @Override
    public PageResult<KnowledgeListItemVO> pageKnowledge(Long userId, Long pageNum, Long pageSize,
                                                         String keyword, Long categoryId, Integer status) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null || !Integer.valueOf(ENABLED_STATUS).equals(user.getStatus())) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND_OR_DISABLED);
        }

        long safePageNum = pageNum == null || pageNum < 1 ? 1 : pageNum;
        long safePageSize = pageSize == null || pageSize < 1 ? 10 : pageSize;
        long offset = (safePageNum - 1) * safePageSize;

        Long total = knowledgeMapper.countKnowledgePage(keyword, categoryId, status);
        List<KnowledgeListItemVO> records = knowledgeMapper.selectKnowledgePage(offset, safePageSize, keyword, categoryId, status);
        long pages = total == 0 ? 0 : (total + safePageSize - 1) / safePageSize;

        return new PageResult<>(records, safePageNum, safePageSize, total, pages);
    }

    @Override
    public KnowledgeDetailVO getKnowledgeDetail(Long userId, Long knowledgeId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null || !Integer.valueOf(ENABLED_STATUS).equals(user.getStatus())) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND_OR_DISABLED);
        }

        KnowledgeDetailVO detail = knowledgeMapper.selectKnowledgeDetail(knowledgeId);
        if (detail == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND);
        }

        return detail;
    }

    @Override
    public KnowledgeDetailVO createKnowledge(Long userId, KnowledgeCreateDTO request) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null || !Integer.valueOf(ENABLED_STATUS).equals(user.getStatus())) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND_OR_DISABLED);
        }

        if (!ADMIN_ROLE.equals(user.getRole())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        KnowledgeCategory category = categoryMapper.selectById(request.getCategoryId());
        if (category == null || !Integer.valueOf(NOT_DELETED).equals(category.getIsDeleted())) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND);
        }

        Knowledge knowledge = new Knowledge();
        knowledge.setCategoryId(request.getCategoryId());
        knowledge.setTitle(request.getTitle());
        knowledge.setKeywords(request.getKeywords());
        knowledge.setContent(request.getContent());
        knowledge.setStatus(request.getStatus());
        knowledge.setIsDeleted(NOT_DELETED);
        knowledge.setCreatedBy(userId);
        knowledge.setUpdatedBy(userId);

        knowledgeMapper.insert(knowledge);

        return knowledgeMapper.selectKnowledgeDetail(knowledge.getId());
    }

    @Override
    public KnowledgeDetailVO updateKnowledge(Long userId, Long knowledgeId, KnowledgeUpdateDTO request) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null || !Integer.valueOf(ENABLED_STATUS).equals(user.getStatus())) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND_OR_DISABLED);
        }

        if (!ADMIN_ROLE.equals(user.getRole())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        Knowledge existing = knowledgeMapper.selectById(knowledgeId);
        if (existing == null || !Integer.valueOf(NOT_DELETED).equals(existing.getIsDeleted())) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND);
        }

        KnowledgeCategory category = categoryMapper.selectById(request.getCategoryId());
        if (category == null || !Integer.valueOf(NOT_DELETED).equals(category.getIsDeleted())) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND);
        }

        Knowledge knowledge = new Knowledge();
        knowledge.setId(knowledgeId);
        knowledge.setCategoryId(request.getCategoryId());
        knowledge.setTitle(request.getTitle());
        knowledge.setKeywords(request.getKeywords());
        knowledge.setContent(request.getContent());
        knowledge.setStatus(request.getStatus());
        knowledge.setUpdatedBy(userId);

        knowledgeMapper.updateById(knowledge);

        return knowledgeMapper.selectKnowledgeDetail(knowledgeId);
    }

    @Override
    public KnowledgeDetailVO updateKnowledgeStatus(Long userId, Long knowledgeId, KnowledgeStatusDTO request) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null || !Integer.valueOf(ENABLED_STATUS).equals(user.getStatus())) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND_OR_DISABLED);
        }

        if (!ADMIN_ROLE.equals(user.getRole())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        Knowledge existing = knowledgeMapper.selectById(knowledgeId);
        if (existing == null || !Integer.valueOf(NOT_DELETED).equals(existing.getIsDeleted())) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND);
        }

        Knowledge knowledge = new Knowledge();
        knowledge.setId(knowledgeId);
        knowledge.setStatus(request.getStatus());
        knowledge.setUpdatedBy(userId);

        knowledgeMapper.updateById(knowledge);

        return knowledgeMapper.selectKnowledgeDetail(knowledgeId);
    }
}