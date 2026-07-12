package com.abhinav.lms.category.service;

import com.abhinav.lms.category.dto.CategoryRequest;
import com.abhinav.lms.category.dto.CategoryResponse;
import com.abhinav.lms.category.entity.Category;
import com.abhinav.lms.category.mapper.CategoryMapper;
import com.abhinav.lms.category.repository.CategoryRepository;
import com.abhinav.lms.exception.DuplicateResourceException;
import com.abhinav.lms.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category category;
    private CategoryRequest categoryRequest;
    private CategoryResponse categoryResponse;
    private UUID categoryId;

    @BeforeEach
    void setUp() {
        categoryId = UUID.randomUUID();

        category = Category.builder()
                .id(categoryId)
                .name("Development")
                .slug("development")
                .build();

        categoryRequest = CategoryRequest.builder()
                .name("Development")
                .build();

        categoryResponse = CategoryResponse.builder()
                .id(categoryId)
                .name("Development")
                .slug("development")
                .build();
    }

    @Test
    void createCategory_Success() {
        when(categoryRepository.existsByName(anyString())).thenReturn(false);
        when(categoryRepository.existsBySlug(anyString())).thenReturn(false);
        when(categoryMapper.toEntity(any(CategoryRequest.class))).thenReturn(category);
        when(categoryRepository.save(any(Category.class))).thenReturn(category);
        when(categoryMapper.toResponse(any(Category.class))).thenReturn(categoryResponse);

        CategoryResponse result = categoryService.createCategory(categoryRequest);

        assertNotNull(result);
        assertEquals("Development", result.getName());
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    void createCategory_ThrowsDuplicateResourceException() {
        when(categoryRepository.existsByName(anyString())).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> categoryService.createCategory(categoryRequest));
    }

    @Test
    void createCategory_WithParentCategory_Success() {
        UUID parentId = UUID.randomUUID();
        categoryRequest.setParentId(parentId);

        Category parentCategory = Category.builder()
                .id(parentId)
                .name("Technology")
                .slug("technology")
                .build();

        when(categoryRepository.existsByName(anyString())).thenReturn(false);
        when(categoryRepository.existsBySlug(anyString())).thenReturn(false);
        when(categoryRepository.findById(parentId)).thenReturn(Optional.of(parentCategory));
        when(categoryMapper.toEntity(any(CategoryRequest.class))).thenReturn(category);
        when(categoryRepository.save(any(Category.class))).thenReturn(category);
        when(categoryMapper.toResponse(any(Category.class))).thenReturn(categoryResponse);

        CategoryResponse result = categoryService.createCategory(categoryRequest);

        assertNotNull(result);
        verify(categoryRepository, times(1)).findById(parentId);
    }

    @Test
    void createCategory_WithParentCategory_ThrowsResourceNotFoundException() {
        UUID parentId = UUID.randomUUID();
        categoryRequest.setParentId(parentId);

        when(categoryRepository.existsByName(anyString())).thenReturn(false);
        when(categoryRepository.existsBySlug(anyString())).thenReturn(false);
        when(categoryMapper.toEntity(any(CategoryRequest.class))).thenReturn(category);
        when(categoryRepository.findById(parentId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> categoryService.createCategory(categoryRequest));
    }

    @Test
    void getCategoryById_Success() {
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(categoryMapper.toResponse(category)).thenReturn(categoryResponse);

        CategoryResponse result = categoryService.getCategoryById(categoryId);

        assertNotNull(result);
        assertEquals(categoryId, result.getId());
    }

    @Test
    void getCategoryBySlug_Success() {
        when(categoryRepository.findBySlug("development")).thenReturn(Optional.of(category));
        when(categoryMapper.toResponse(category)).thenReturn(categoryResponse);

        CategoryResponse result = categoryService.getCategoryBySlug("development");

        assertNotNull(result);
        assertEquals("development", result.getSlug());
    }
}
