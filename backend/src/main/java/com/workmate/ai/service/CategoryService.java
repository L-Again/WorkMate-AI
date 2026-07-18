package com.workmate.ai.service;

import com.workmate.ai.vo.CategoryVO;
import com.workmate.ai.dto.CategoryCreateDTO;
import com.workmate.ai.dto.CategoryUpdateDTO;

import java.util.List;

public interface CategoryService {

    List<CategoryVO> listCategories(Long userId, Boolean includeDisabled);

    CategoryVO getCategoryDetail(Long userId, Long categoryId);

    CategoryVO createCategory(Long userId, CategoryCreateDTO request);

    CategoryVO updateCategory(Long userId, Long categoryId, CategoryUpdateDTO request);

}