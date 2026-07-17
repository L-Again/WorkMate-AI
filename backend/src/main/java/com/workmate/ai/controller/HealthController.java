package com.workmate.ai.controller;

import com.workmate.ai.common.CommonResult;
import com.workmate.ai.vo.HealthVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/api/health")
    public CommonResult<HealthVO> health() {
        return CommonResult.success(new HealthVO("UP"));
    }
}