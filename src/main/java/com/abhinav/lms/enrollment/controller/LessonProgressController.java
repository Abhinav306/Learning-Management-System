package com.abhinav.lms.enrollment.controller;

import com.abhinav.lms.common.constants.AppConstants;
import com.abhinav.lms.common.dto.ApiResponse;
import com.abhinav.lms.enrollment.dto.CourseProgressResponse;
import com.abhinav.lms.enrollment.service.LessonProgressService;
import com.abhinav.lms.security.model.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping(AppConstants.API_V1 + "/enrollments/{enrollmentId}")
@RequiredArgsConstructor
@Tag(name = "Lesson Progress Management", description = "Endpoints for tracking student lesson completion status")
public class LessonProgressController {

    private final LessonProgressService lessonProgressService;

    @PostMapping("/lessons/{lessonId}/complete")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    @Operation(summary = "Mark a lesson as complete for an enrollment (Student owner / Admin only)")
    public ApiResponse<CourseProgressResponse> markLessonComplete(
            @PathVariable UUID enrollmentId,
            @PathVariable UUID lessonId,
            @AuthenticationPrincipal UserPrincipal principal) {
        CourseProgressResponse response = lessonProgressService.markLessonComplete(enrollmentId, lessonId, principal);
        return ApiResponse.success(response, "Lesson marked as complete successfully");
    }

    @DeleteMapping("/lessons/{lessonId}/complete")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    @Operation(summary = "Mark a lesson as incomplete (Student owner / Admin only)")
    public ApiResponse<CourseProgressResponse> markLessonIncomplete(
            @PathVariable UUID enrollmentId,
            @PathVariable UUID lessonId,
            @AuthenticationPrincipal UserPrincipal principal) {
        CourseProgressResponse response = lessonProgressService.markLessonIncomplete(enrollmentId, lessonId, principal);
        return ApiResponse.success(response, "Lesson marked as incomplete successfully");
    }

    @GetMapping("/progress")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    @Operation(summary = "Get overall course completion progress metrics (Student owner / Admin only)")
    public ApiResponse<CourseProgressResponse> getCourseProgress(
            @PathVariable UUID enrollmentId,
            @AuthenticationPrincipal UserPrincipal principal) {
        CourseProgressResponse response = lessonProgressService.getCourseProgress(enrollmentId, principal);
        return ApiResponse.success(response);
    }
}
