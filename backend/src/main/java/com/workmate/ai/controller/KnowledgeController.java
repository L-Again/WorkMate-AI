package com.workmate.ai.controller;

import com.workmate.ai.common.CommonResult;
import com.workmate.ai.common.PageResult;
import com.workmate.ai.service.KnowledgeService;
import com.workmate.ai.vo.KnowledgeListItemVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
}