package com.workmate.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.workmate.ai.entity.ChatMessage;
import com.workmate.ai.vo.MessageVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {

    List<MessageVO> selectMessagesBySession(@Param("sessionId") Long sessionId,
                                            @Param("offset") Long offset,
                                            @Param("pageSize") Long pageSize);

    Long countMessagesBySession(@Param("sessionId") Long sessionId);
}