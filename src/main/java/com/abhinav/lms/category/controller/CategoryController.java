package com.abhinav.lms.category.controller;

import com.abhinav.lms.category.dto.CategoryRequest;
import com.abhinav.lms.category.dto.CategoryResponse;
import com.abhinav.lms.category.service.CategoryService;
import com.abhinav.lms.common.constants.AppConstants;
import com.abhinav.lms.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(AppConstants.API_V1 + "/categories")
@RequiredArgsConstructor
@Tag(name = "Category Management", description = "Endpoints for hierarchical course categories (public reads, admin writes)")
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new course category (Admin only)")
    public ApiResponse<CategoryResponse> createCategory(@Valid @RequestBody CategoryRequest request) {
        CategoryResponse response = categoryService.createCategory(request);
        return ApiResponse.created(response, "Category created successfully");
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID (Public)")
    public ApiResponse<CategoryResponse> getCategoryById(@PathVariable UUID id) {
        CategoryResponse response = categoryService.getCategoryById(id);
        return ApiResponse.success(response);
    }

    @GetMapping("/slug/{slug}")
    @Operation(summary = "Get category by slug (Public)")
    public ApiResponse<CategoryResponse> getCategoryBySlug(@PathVariable String slug) {
        CategoryResponse response = categoryService.getCategoryBySlug(slug);
        return ApiResponse.success(response);
    }

    @GetMapping
    @Operation(summary = "Get all categories (Public)")
    public ApiResponse<List<CategoryResponse>> getAllCategories() {
        List<CategoryResponse> response = categoryService.getAllCategories();
        return ApiResponse.success(response);
    }

    @GetMapping("/root")
    @Operation(summary = "Get all top-level root categories (Public)")
    public ApiResponse<List<CategoryResponse>> getRootCategories() {
        List<CategoryResponse> response = categoryService.getRootCategories();
        return ApiResponse.success(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update an existing category (Admin only)")
    public ApiResponse<CategoryResponse> updateCategory(
            @PathVariable UUID id,
            @Valid @RequestBody CategoryRequest request) {
        CategoryResponse response = categoryService.updateCategory(id, request);
        return ApiResponse.success(response, "Category updated successfully");
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete an existing category (Admin only)")
    public void deleteCategory(@PathVariable UUID id) {
        categoryService.deleteCategory(id);
    }
}
