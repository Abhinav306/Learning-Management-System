package com.abhinav.lms.category.service;

import com.abhinav.lms.category.dto.CategoryRequest;
import com.abhinav.lms.category.dto.CategoryResponse;

import java.util.List;
import java.util.UUID;

public interface CategoryService {

    CategoryResponse createCategory(CategoryRequest request);

    CategoryResponse getCategoryById(UUID id);

    CategoryResponse getCategoryBySlug(String slug);

    List<CategoryResponse> getAllCategories();

    List<CategoryResponse> getRootCategories();

    CategoryResponse updateCategory(UUID id, CategoryRequest request);

    void deleteCategory(UUID id);
}
