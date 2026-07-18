package com.workmate.ai.service;

import com.workmate.ai.common.PageResult;
import com.workmate.ai.vo.KnowledgeListItemVO;

public interface KnowledgeService {

    PageResult<KnowledgeListItemVO> pageKnowledge(Long userId, Long pageNum, Long pageSize,
                                                  String keyword, Long categoryId, Integer status);
}