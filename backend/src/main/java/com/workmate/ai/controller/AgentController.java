package com.workmate.ai.controller;

import com.workmate.ai.common.CommonResult;
import com.workmate.ai.dto.AgentChatDTO;
import com.workmate.ai.service.AgentService;
import com.workmate.ai.vo.AgentAnswerVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AgentController {

    private final AgentService agentService;

    public AgentController(AgentService agentService) {
        this.agentService = agentService;
    }

    @PostMapping("/api/agent/chat")
    public CommonResult<AgentAnswerVO> chat(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody AgentChatDTO request) {
        return CommonResult.success(agentService.chat(userId, request));
    }
}