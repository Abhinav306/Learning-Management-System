package com.abhinav.lms.category.service;

import com.abhinav.lms.category.dto.CategoryRequest;
import com.abhinav.lms.category.dto.CategoryResponse;
import com.abhinav.lms.category.entity.Category;
import com.abhinav.lms.category.mapper.CategoryMapper;
import com.abhinav.lms.category.repository.CategoryRepository;
import com.abhinav.lms.exception.BusinessException;
import com.abhinav.lms.exception.DuplicateResourceException;
import com.abhinav.lms.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        log.info("Creating new category: {}", request.getName());

        if (categoryRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Category", "name", request.getName());
        }

        String slug = generateSlug(request.getName());
        if (categoryRepository.existsBySlug(slug)) {
            throw new DuplicateResourceException("Category", "slug", slug);
        }

        Category category = categoryMapper.toEntity(request);
        category.setSlug(slug);

        if (request.getParentId() != null) {
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "parentId", request.getParentId()));
            category.setParent(parent);
        }

        Category savedCategory = categoryRepository.save(category);
        log.info("Category created successfully with ID: {}", savedCategory.getId());
        return categoryMapper.toResponse(savedCategory);
    }

    @Override
    public CategoryResponse getCategoryById(UUID id) {
        log.debug("Fetching category by id: {}", id);
        Category category = findCategoryByIdOrThrow(id);
        return categoryMapper.toResponse(category);
    }

    @Override
    public CategoryResponse getCategoryBySlug(String slug) {
        log.debug("Fetching category by slug: {}", slug);
        Category category = categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "slug", slug));
        return categoryMapper.toResponse(category);
    }

    @Override
    public List<CategoryResponse> getAllCategories() {
        log.debug("Fetching all categories");
        List<Category> categories = categoryRepository.findAll();
        return categoryMapper.toResponseList(categories);
    }

    @Override
    public List<CategoryResponse> getRootCategories() {
        log.debug("Fetching root categories");
        List<Category> categories = categoryRepository.findByParentIsNull();
        return categoryMapper.toResponseList(categories);
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(UUID id, CategoryRequest request) {
        log.info("Updating category with ID: {}", id);
        Category category = findCategoryByIdOrThrow(id);

        // Check if name is being changed and if new name already exists
        if (!category.getName().equals(request.getName())) {
            if (categoryRepository.existsByName(request.getName())) {
                throw new DuplicateResourceException("Category", "name", request.getName());
            }
            String newSlug = generateSlug(request.getName());
            if (categoryRepository.existsBySlug(newSlug)) {
                throw new DuplicateResourceException("Category", "slug", newSlug);
            }
            category.setSlug(newSlug);
        }

        categoryMapper.updateEntityFromRequest(request, category);

        if (request.getParentId() != null) {
            if (request.getParentId().equals(id)) {
                throw new BusinessException("A category cannot be its own parent");
            }
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "parentId", request.getParentId()));
            category.setParent(parent);
        } else {
            category.setParent(null);
        }

        Category updatedCategory = categoryRepository.save(category);
        log.info("Category updated successfully with ID: {}", updatedCategory.getId());
        return categoryMapper.toResponse(updatedCategory);
    }

    @Override
    @Transactional
    public void deleteCategory(UUID id) {
        log.info("Deleting category with ID: {}", id);
        Category category = findCategoryByIdOrThrow(id);

        if (!category.getChildren().isEmpty()) {
            throw new BusinessException("Cannot delete category as it has subcategories associated with it");
        }

        categoryRepository.delete(category);
        log.info("Category deleted successfully with ID: {}", id);
    }

    // ═══════════════════════ Private Helpers ═══════════════════════

    private Category findCategoryByIdOrThrow(UUID id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
    }

    private String generateSlug(String input) {
        if (input == null) {
            return "";
        }
        return input.toLowerCase(Locale.ENGLISH)
                .replaceAll("[^a-z0-9\\s-]", "") // Remove all non-alphanumeric except spaces and hyphens
                .replaceAll("\\s+", "-")       // Replace spaces with hyphens
                .replaceAll("-+", "-")         // Collapse duplicate hyphens
                .trim();
    }
}
