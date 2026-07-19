package com.workmate.ai.controller;

import com.workmate.ai.common.CommonResult;
import com.workmate.ai.common.PageResult;
import com.workmate.ai.service.ModelCallLogService;
import com.workmate.ai.vo.ModelCallLogVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ModelCallLogController {

    private final ModelCallLogService modelCallLogService;

    public ModelCallLogController(ModelCallLogService modelCallLogService) {
        this.modelCallLogService = modelCallLogService;
    }

    @GetMapping("/api/admin/model-logs")
    public CommonResult<PageResult<ModelCallLogVO>> pageModelCallLogs(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "20") Long pageSize,
            @RequestParam(required = false) String callStatus) {
        return CommonResult.success(modelCallLogService.pageModelCallLogs(userId, pageNum, pageSize, callStatus));
    }
}