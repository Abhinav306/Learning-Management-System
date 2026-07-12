package com.abhinav.lms.assignment.controller;

import com.abhinav.lms.assignment.dto.AssignmentRequest;
import com.abhinav.lms.assignment.dto.AssignmentResponse;
import com.abhinav.lms.assignment.dto.GradeRequest;
import com.abhinav.lms.assignment.dto.SubmissionRequest;
import com.abhinav.lms.assignment.dto.SubmissionResponse;
import com.abhinav.lms.assignment.service.AssignmentService;
import com.abhinav.lms.common.constants.AppConstants;
import com.abhinav.lms.common.dto.ApiResponse;
import com.abhinav.lms.common.dto.PagedResponse;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Assignment Management", description = "Endpoints for creating assignments, submissions, and grading")
public class AssignmentController {

    private final AssignmentService assignmentService;

    @PostMapping(AppConstants.API_V1 + "/courses/{courseId}/assignments")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @Operation(summary = "Create a new assignment in a course (Instructor owner / Admin only)")
    public ApiResponse<AssignmentResponse> createAssignment(
            @PathVariable UUID courseId,
            @Valid @RequestBody AssignmentRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        AssignmentResponse response = assignmentService.createAssignment(courseId, request, principal);
        return ApiResponse.created(response, "Assignment created successfully");
    }

    @GetMapping(AppConstants.API_V1 + "/assignments/{assignmentId}")
    @Operation(summary = "Get assignment by ID (Public)")
    public ApiResponse<AssignmentResponse> getAssignmentById(@PathVariable UUID assignmentId) {
        AssignmentResponse response = assignmentService.getAssignmentById(assignmentId);
        return ApiResponse.success(response);
    }

    @GetMapping(AppConstants.API_V1 + "/courses/{courseId}/assignments")
    @Operation(summary = "Get all assignments in a course (Public)")
    public ApiResponse<List<AssignmentResponse>> getAssignmentsByCourse(@PathVariable UUID courseId) {
        List<AssignmentResponse> response = assignmentService.getAssignmentsByCourse(courseId);
        return ApiResponse.success(response);
    }

    @PutMapping(AppConstants.API_V1 + "/assignments/{assignmentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @Operation(summary = "Update an existing assignment details (Instructor owner / Admin only)")
    public ApiResponse<AssignmentResponse> updateAssignment(
            @PathVariable UUID assignmentId,
            @Valid @RequestBody AssignmentRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        AssignmentResponse response = assignmentService.updateAssignment(assignmentId, request, principal);
        return ApiResponse.success(response, "Assignment updated successfully");
    }

    @DeleteMapping(AppConstants.API_V1 + "/assignments/{assignmentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @Operation(summary = "Delete an assignment (Instructor owner / Admin only)")
    public void deleteAssignment(
            @PathVariable UUID assignmentId,
            @AuthenticationPrincipal UserPrincipal principal) {
        assignmentService.deleteAssignment(assignmentId, principal);
    }

    @PostMapping(AppConstants.API_V1 + "/assignments/{assignmentId}/submissions")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    @Operation(summary = "Submit coursework for an assignment (Enrolled Student / Admin only)")
    public ApiResponse<SubmissionResponse> submitAssignment(
            @PathVariable UUID assignmentId,
            @Valid @RequestBody SubmissionRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        SubmissionResponse response = assignmentService.submitAssignment(assignmentId, request, principal);
        return ApiResponse.created(response, "Assignment submitted successfully");
    }

    @PutMapping(AppConstants.API_V1 + "/assignments/{assignmentId}/submissions/{submissionId}/grade")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @Operation(summary = "Grade student coursework submission (Instructor owner / Admin only)")
    public ApiResponse<SubmissionResponse> gradeSubmission(
            @PathVariable UUID assignmentId,
            @PathVariable UUID submissionId,
            @Valid @RequestBody GradeRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        SubmissionResponse response = assignmentService.gradeSubmission(assignmentId, submissionId, request, principal);
        return ApiResponse.success(response, "Submission graded successfully");
    }

    @GetMapping(AppConstants.API_V1 + "/assignments/{assignmentId}/submissions")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @Operation(summary = "Get all submissions for an assignment (Instructor owner / Admin only)")
    public ApiResponse<PagedResponse<SubmissionResponse>> getAssignmentSubmissions(
            @PathVariable UUID assignmentId,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size,
            @AuthenticationPrincipal UserPrincipal principal) {
        PagedResponse<SubmissionResponse> response = assignmentService.getAssignmentSubmissions(assignmentId, page, size, principal);
        return ApiResponse.success(response);
    }

    @GetMapping(AppConstants.API_V1 + "/assignments/{assignmentId}/my-submission")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    @Operation(summary = "Get current student's coursework submission (Student owner / Admin only)")
    public ApiResponse<SubmissionResponse> getMySubmission(
            @PathVariable UUID assignmentId,
            @AuthenticationPrincipal UserPrincipal principal) {
        SubmissionResponse response = assignmentService.getMySubmission(assignmentId, principal);
        return ApiResponse.success(response);
    }
}
