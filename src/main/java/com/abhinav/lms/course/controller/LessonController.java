package com.abhinav.lms.course.controller;

import com.abhinav.lms.common.constants.AppConstants;
import com.abhinav.lms.common.dto.ApiResponse;
import com.abhinav.lms.course.dto.LessonReorderRequest;
import com.abhinav.lms.course.dto.LessonRequest;
import com.abhinav.lms.course.dto.LessonResponse;
import com.abhinav.lms.course.service.LessonService;
import com.abhinav.lms.security.model.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(AppConstants.API_V1 + "/courses/{courseId}/sections/{sectionId}/lessons")
@RequiredArgsConstructor
@Tag(name = "Lesson Management", description = "Endpoints for managing lessons within a course section")
public class LessonController {

    private final LessonService lessonService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @Operation(summary = "Create a new lesson in a section (Instructor owner / Admin only)")
    public ApiResponse<LessonResponse> createLesson(
            @PathVariable UUID courseId,
            @PathVariable UUID sectionId,
            @Valid @RequestBody LessonRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        LessonResponse response = lessonService.createLesson(courseId, sectionId, request, principal);
        return ApiResponse.created(response, "Lesson created successfully");
    }

    @GetMapping("/{lessonId}")
    @Operation(summary = "Get lesson by ID (Public)")
    public ApiResponse<LessonResponse> getLessonById(
            @PathVariable UUID courseId,
            @PathVariable UUID sectionId,
            @PathVariable UUID lessonId) {
        LessonResponse response = lessonService.getLessonById(courseId, sectionId, lessonId);
        return ApiResponse.success(response);
    }

    @GetMapping
    @Operation(summary = "Get all lessons in a section (Public)")
    public ApiResponse<List<LessonResponse>> getLessonsBySection(
            @PathVariable UUID courseId,
            @PathVariable UUID sectionId) {
        List<LessonResponse> response = lessonService.getLessonsBySection(courseId, sectionId);
        return ApiResponse.success(response);
    }

    @PutMapping("/{lessonId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @Operation(summary = "Update an existing lesson (Instructor owner / Admin only)")
    public ApiResponse<LessonResponse> updateLesson(
            @PathVariable UUID courseId,
            @PathVariable UUID sectionId,
            @PathVariable UUID lessonId,
            @Valid @RequestBody LessonRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        LessonResponse response = lessonService.updateLesson(courseId, sectionId, lessonId, request, principal);
        return ApiResponse.success(response, "Lesson updated successfully");
    }

    @DeleteMapping("/{lessonId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @Operation(summary = "Delete a lesson (Instructor owner / Admin only)")
    public void deleteLesson(
            @PathVariable UUID courseId,
            @PathVariable UUID sectionId,
            @PathVariable UUID lessonId,
            @AuthenticationPrincipal UserPrincipal principal) {
        lessonService.deleteLesson(courseId, sectionId, lessonId, principal);
    }

    @PostMapping("/reorder")
    @Operation(summary = "Reorder lessons within a section (Instructor owner / Admin only)")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ApiResponse<Void> reorderLessons(
            @PathVariable UUID courseId,
            @PathVariable UUID sectionId,
            @Valid @RequestBody LessonReorderRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        lessonService.reorderLessons(courseId, sectionId, request.getLessonIds(), principal);
        return ApiResponse.success(null, "Lessons reordered successfully");
    }
}
