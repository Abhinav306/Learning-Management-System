package com.abhinav.lms.course.controller;

import com.abhinav.lms.common.constants.AppConstants;
import com.abhinav.lms.common.dto.ApiResponse;
import com.abhinav.lms.common.dto.PagedResponse;
import com.abhinav.lms.course.dto.CourseRequest;
import com.abhinav.lms.course.dto.CourseResponse;
import com.abhinav.lms.course.entity.CourseStatus;
import com.abhinav.lms.course.entity.DifficultyLevel;
import com.abhinav.lms.course.service.CourseService;
import com.abhinav.lms.security.model.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping(AppConstants.API_V1 + "/courses")
@RequiredArgsConstructor
@Tag(name = "Course Management", description = "Endpoints for creating and browsing courses")
public class CourseController {

    private final CourseService courseService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @Operation(summary = "Create a new course (Instructor/Admin only)")
    public ApiResponse<CourseResponse> createCourse(
            @Valid @RequestBody CourseRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        CourseResponse response = courseService.createCourse(request, principal);
        return ApiResponse.created(response, "Course created successfully");
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get course by ID (Public)")
    public ApiResponse<CourseResponse> getCourseById(@PathVariable UUID id) {
        CourseResponse response = courseService.getCourseById(id);
        return ApiResponse.success(response);
    }

    @GetMapping
    @Operation(summary = "Get all courses paginated (Public)")
    public ApiResponse<PagedResponse<CourseResponse>> getAllCourses(
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_BY) String sortBy,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_DIR) String sortDir) {
        PagedResponse<CourseResponse> response = courseService.getAllCourses(page, size, sortBy, sortDir);
        return ApiResponse.success(response);
    }

    @GetMapping("/search")
    @Operation(summary = "Filter and search courses dynamically (Public)")
    public ApiResponse<PagedResponse<CourseResponse>> searchCourses(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) DifficultyLevel difficulty,
            @RequestParam(required = false) CourseStatus status,
            @RequestParam(required = false) UUID instructorId,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
        PagedResponse<CourseResponse> response = courseService.searchCourses(
                keyword, categoryId, difficulty, status, instructorId, page, size);
        return ApiResponse.success(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @Operation(summary = "Update an existing course details (Instructor owner / Admin only)")
    public ApiResponse<CourseResponse> updateCourse(
            @PathVariable UUID id,
            @Valid @RequestBody CourseRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        CourseResponse response = courseService.updateCourse(id, request, principal);
        return ApiResponse.success(response, "Course updated successfully");
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @Operation(summary = "Delete a course (Instructor owner / Admin only)")
    public void deleteCourse(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal) {
        courseService.deleteCourse(id, principal);
    }
}
