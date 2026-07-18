package com.workmate.ai.service.impl;

import com.workmate.ai.common.ErrorCode;
import com.workmate.ai.common.PageResult;
import com.workmate.ai.entity.SysUser;
import com.workmate.ai.exception.BusinessException;
import com.workmate.ai.mapper.KnowledgeMapper;
import com.workmate.ai.mapper.SysUserMapper;
import com.workmate.ai.service.KnowledgeService;
import com.workmate.ai.vo.KnowledgeListItemVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KnowledgeServiceImpl implements KnowledgeService {

    private static final int ENABLED_STATUS = 1;

    private final SysUserMapper sysUserMapper;
    private final KnowledgeMapper knowledgeMapper;

    public KnowledgeServiceImpl(SysUserMapper sysUserMapper, KnowledgeMapper knowledgeMapper) {
        this.sysUserMapper = sysUserMapper;
        this.knowledgeMapper = knowledgeMapper;
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
}