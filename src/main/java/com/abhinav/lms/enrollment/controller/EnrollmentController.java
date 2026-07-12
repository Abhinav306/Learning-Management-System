package com.abhinav.lms.enrollment.controller;

import com.abhinav.lms.common.constants.AppConstants;
import com.abhinav.lms.common.dto.ApiResponse;
import com.abhinav.lms.common.dto.PagedResponse;
import com.abhinav.lms.enrollment.dto.EnrollmentResponse;
import com.abhinav.lms.enrollment.service.EnrollmentService;
import com.abhinav.lms.security.model.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Enrollment Management", description = "Endpoints for student course registrations and rosters")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @PostMapping(AppConstants.API_V1 + "/courses/{courseId}/enroll")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    @Operation(summary = "Enroll in a course (Student/Admin only)")
    public ApiResponse<EnrollmentResponse> enrollCourse(
            @PathVariable UUID courseId,
            @AuthenticationPrincipal UserPrincipal principal) {
        EnrollmentResponse response = enrollmentService.enrollCourse(courseId, principal);
        return ApiResponse.created(response, "Enrolled in course successfully");
    }

    @DeleteMapping(AppConstants.API_V1 + "/courses/{courseId}/enroll")
    @Operation(summary = "Drop out of a course (Student/Admin only)")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    public ApiResponse<EnrollmentResponse> dropCourse(
            @PathVariable UUID courseId,
            @AuthenticationPrincipal UserPrincipal principal) {
        EnrollmentResponse response = enrollmentService.dropCourse(courseId, principal);
        return ApiResponse.success(response, "Dropped course successfully");
    }

    @GetMapping(AppConstants.API_V1 + "/enrollments")
    @Operation(summary = "Get current student's course enrollments (Student/Admin only)")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    public ApiResponse<PagedResponse<EnrollmentResponse>> getMyEnrollments(
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size,
            @AuthenticationPrincipal UserPrincipal principal) {
        PagedResponse<EnrollmentResponse> response = enrollmentService.getStudentEnrollments(principal, page, size);
        return ApiResponse.success(response);
    }

    @GetMapping(AppConstants.API_V1 + "/courses/{courseId}/enrollments")
    @Operation(summary = "Get course enrollment roster (Instructor owner / Admin only)")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ApiResponse<PagedResponse<EnrollmentResponse>> getCourseRoster(
            @PathVariable UUID courseId,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size,
            @AuthenticationPrincipal UserPrincipal principal) {
        PagedResponse<EnrollmentResponse> response = enrollmentService.getCourseEnrollments(courseId, principal, page, size);
        return ApiResponse.success(response);
    }
}
