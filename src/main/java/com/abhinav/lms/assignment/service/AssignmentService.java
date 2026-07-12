package com.abhinav.lms.assignment.service;

import com.abhinav.lms.assignment.dto.AssignmentRequest;
import com.abhinav.lms.assignment.dto.AssignmentResponse;
import com.abhinav.lms.assignment.dto.GradeRequest;
import com.abhinav.lms.assignment.dto.SubmissionRequest;
import com.abhinav.lms.assignment.dto.SubmissionResponse;
import com.abhinav.lms.common.dto.PagedResponse;
import com.abhinav.lms.security.model.UserPrincipal;

import java.util.List;
import java.util.UUID;

public interface AssignmentService {

    AssignmentResponse createAssignment(UUID courseId, AssignmentRequest request, UserPrincipal currentUser);

    AssignmentResponse getAssignmentById(UUID assignmentId);

    List<AssignmentResponse> getAssignmentsByCourse(UUID courseId);

    AssignmentResponse updateAssignment(UUID assignmentId, AssignmentRequest request, UserPrincipal currentUser);

    void deleteAssignment(UUID assignmentId, UserPrincipal currentUser);

    SubmissionResponse submitAssignment(UUID assignmentId, SubmissionRequest request, UserPrincipal currentUser);

    SubmissionResponse gradeSubmission(UUID assignmentId, UUID submissionId, GradeRequest request, UserPrincipal currentUser);

    PagedResponse<SubmissionResponse> getAssignmentSubmissions(UUID assignmentId, int page, int size, UserPrincipal currentUser);

    SubmissionResponse getMySubmission(UUID assignmentId, UserPrincipal currentUser);
}
