package com.workmate.ai.service;

import com.workmate.ai.vo.CategoryVO;

import java.util.List;

public interface CategoryService {

    List<CategoryVO> listCategories(Long userId, Boolean includeDisabled);
}