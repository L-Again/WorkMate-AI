package com.workmate.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.workmate.ai.entity.ModelCallLog;
import com.workmate.ai.vo.ModelCallLogVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ModelCallLogMapper extends BaseMapper<ModelCallLog> {

    Long countModelCallLogs(@Param("callStatus") String callStatus);

    List<ModelCallLogVO> selectModelCallLogPage(@Param("callStatus") String callStatus,
                                                @Param("offset") Long offset,
                                                @Param("pageSize") Long pageSize);
}