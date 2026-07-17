package com.workmate.ai.controller;

import com.workmate.ai.common.CommonResult;
import com.workmate.ai.service.CategoryService;
import com.workmate.ai.vo.CategoryVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/api/knowledge/categories")
    public CommonResult<List<CategoryVO>> listCategories(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(defaultValue = "false") Boolean includeDisabled) {
        return CommonResult.success(categoryService.listCategories(userId, includeDisabled));
    }
}