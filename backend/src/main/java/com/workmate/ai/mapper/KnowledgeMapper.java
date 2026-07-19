package com.workmate.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.workmate.ai.entity.Knowledge;
import com.workmate.ai.vo.KnowledgeListItemVO;
import com.workmate.ai.vo.KnowledgeDetailVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface KnowledgeMapper extends BaseMapper<Knowledge> {

    List<KnowledgeListItemVO> selectKnowledgePage(@Param("offset") Long offset,
                                                  @Param("pageSize") Long pageSize,
                                                  @Param("keyword") String keyword,
                                                  @Param("categoryId") Long categoryId,
                                                  @Param("status") Integer status);

    Long countKnowledgePage(@Param("keyword") String keyword,
                            @Param("categoryId") Long categoryId,
                            @Param("status") Integer status);

    KnowledgeDetailVO selectKnowledgeDetail(@Param("id") Long id);

    List<KnowledgeListItemVO> searchEffectiveKnowledge(@Param("keyword") String keyword,
                                                       @Param("limit") Integer limit);
}