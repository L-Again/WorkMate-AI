package com.workmate.ai.controller;

import com.workmate.ai.common.CommonResult;
import com.workmate.ai.common.PageResult;
import com.workmate.ai.service.KnowledgeService;
import com.workmate.ai.vo.KnowledgeListItemVO;
import com.workmate.ai.vo.KnowledgeDetailVO;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.workmate.ai.dto.KnowledgeCreateDTO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.workmate.ai.dto.KnowledgeUpdateDTO;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
public class KnowledgeController {

    private final KnowledgeService knowledgeService;

    public KnowledgeController(KnowledgeService knowledgeService) {
        this.knowledgeService = knowledgeService;
    }

    @GetMapping("/api/knowledge")
    public CommonResult<PageResult<KnowledgeListItemVO>> pageKnowledge(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "10") Long pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer status) {
        return CommonResult.success(knowledgeService.pageKnowledge(userId, pageNum, pageSize, keyword, categoryId, status));
    }

    @GetMapping("/api/knowledge/{id}")
    public CommonResult<KnowledgeDetailVO> getKnowledgeDetail(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable("id") Long knowledgeId) {
        return CommonResult.success(knowledgeService.getKnowledgeDetail(userId, knowledgeId));
    }

    @PostMapping("/api/knowledge")
    public CommonResult<KnowledgeDetailVO> createKnowledge(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody KnowledgeCreateDTO request) {
        return CommonResult.success(knowledgeService.createKnowledge(userId, request));
    }

    @PutMapping("/api/knowledge/{id}")
    public CommonResult<KnowledgeDetailVO> updateKnowledge(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable("id") Long knowledgeId,
            @Valid @RequestBody KnowledgeUpdateDTO request) {
        return CommonResult.success(knowledgeService.updateKnowledge(userId, knowledgeId, request));
    }
}