package com.workmate.ai.service;

import com.workmate.ai.common.PageResult;
import com.workmate.ai.dto.ModelCallLogCreateDTO;
import com.workmate.ai.vo.ModelCallLogVO;

public interface ModelCallLogService {

    void recordAsync(ModelCallLogCreateDTO request);

    PageResult<ModelCallLogVO> pageModelCallLogs(Long userId, Long pageNum, Long pageSize, String callStatus);
}