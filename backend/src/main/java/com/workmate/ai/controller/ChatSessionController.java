package com.workmate.ai.controller;

import com.workmate.ai.common.CommonResult;
import com.workmate.ai.dto.SessionCreateDTO;
import com.workmate.ai.dto.SessionTitleUpdateDTO;
import com.workmate.ai.service.ChatSessionService;
import com.workmate.ai.vo.SessionVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import com.workmate.ai.common.PageResult;
import com.workmate.ai.vo.SessionListVO;
import org.springframework.web.bind.annotation.RequestParam;
import com.workmate.ai.vo.MessageVO;

@RestController
public class ChatSessionController {

    private final ChatSessionService chatSessionService;

    public ChatSessionController(ChatSessionService chatSessionService) {
        this.chatSessionService = chatSessionService;
    }

    @PostMapping("/api/chat/sessions")
    public CommonResult<SessionVO> createSession(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody SessionCreateDTO request) {
        return CommonResult.success(chatSessionService.createSession(userId, request));
    }

    @GetMapping("/api/chat/sessions")
    public CommonResult<PageResult<SessionListVO>> listSessions(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "20") Long pageSize) {
        return CommonResult.success(chatSessionService.listSessions(userId, pageNum, pageSize));
    }

    @GetMapping("/api/chat/sessions/{sessionId}/messages")
    public CommonResult<PageResult<MessageVO>> listMessages(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long sessionId,
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "50") Long pageSize) {
        return CommonResult.success(chatSessionService.listMessages(userId, sessionId, pageNum, pageSize));
    }

    @GetMapping("/api/chat/sessions/{sessionId}")
    public CommonResult<SessionVO> getSessionDetail(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long sessionId) {
        return CommonResult.success(chatSessionService.getSessionDetail(userId, sessionId));
    }

    @PatchMapping("/api/chat/sessions/{sessionId}/title")
    public CommonResult<SessionVO> updateSessionTitle(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long sessionId,
            @Valid @RequestBody SessionTitleUpdateDTO request) {
        return CommonResult.success(chatSessionService.updateSessionTitle(userId, sessionId, request));
    }

    @DeleteMapping("/api/chat/sessions/{sessionId}")
    public CommonResult<Boolean> deleteSession(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long sessionId) {
        return CommonResult.success(chatSessionService.deleteSession(userId, sessionId));
    }
}