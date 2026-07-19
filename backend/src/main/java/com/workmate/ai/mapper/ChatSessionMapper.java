package com.workmate.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.workmate.ai.entity.ChatSession;
import com.workmate.ai.vo.SessionListVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ChatSessionMapper extends BaseMapper<ChatSession> {

    List<SessionListVO> selectSessionPage(@Param("userId") Long userId,
                                          @Param("offset") Long offset,
                                          @Param("pageSize") Long pageSize);

    Long countActiveSessions(@Param("userId") Long userId);
}