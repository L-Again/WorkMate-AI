package com.workmate.ai.controller;

import com.workmate.ai.common.CommonResult;
import com.workmate.ai.service.CategoryService;
import com.workmate.ai.vo.CategoryVO;
import com.workmate.ai.dto.CategoryCreateDTO;
import com.workmate.ai.dto.CategoryUpdateDTO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.workmate.ai.dto.CategoryStatusDTO;
import org.springframework.web.bind.annotation.PatchMapping;
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

    @GetMapping("/api/knowledge/categories/{id}")
    public CommonResult<CategoryVO> getCategoryDetail(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable("id") Long categoryId) {
        return CommonResult.success(categoryService.getCategoryDetail(userId, categoryId));
    }

    @PostMapping("/api/knowledge/categories")
    public CommonResult<CategoryVO> createCategory(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody CategoryCreateDTO request) {
        return CommonResult.success(categoryService.createCategory(userId, request));
    }

    @PutMapping("/api/knowledge/categories/{id}")
    public CommonResult<CategoryVO> updateCategory(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable("id") Long categoryId,
            @Valid @RequestBody CategoryUpdateDTO request) {
        return CommonResult.success(categoryService.updateCategory(userId, categoryId, request));
    }

    @PatchMapping("/api/knowledge/categories/{id}/status")
    public CommonResult<CategoryVO> updateCategoryStatus(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable("id") Long categoryId,
            @Valid @RequestBody CategoryStatusDTO request) {
        return CommonResult.success(categoryService.updateCategoryStatus(userId, categoryId, request));
    }

}