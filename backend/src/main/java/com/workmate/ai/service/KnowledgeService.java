package com.workmate.ai.service;

import com.workmate.ai.common.PageResult;
import com.workmate.ai.vo.KnowledgeListItemVO;
import com.workmate.ai.vo.KnowledgeDetailVO;
import com.workmate.ai.dto.KnowledgeCreateDTO;
import com.workmate.ai.dto.KnowledgeUpdateDTO;

public interface KnowledgeService {

    PageResult<KnowledgeListItemVO> pageKnowledge(Long userId, Long pageNum, Long pageSize,

                                                  String keyword, Long categoryId, Integer status);

    KnowledgeDetailVO getKnowledgeDetail(Long userId, Long knowledgeId);

    KnowledgeDetailVO createKnowledge(Long userId, KnowledgeCreateDTO request);

    KnowledgeDetailVO updateKnowledge(Long userId, Long knowledgeId, KnowledgeUpdateDTO request);


}