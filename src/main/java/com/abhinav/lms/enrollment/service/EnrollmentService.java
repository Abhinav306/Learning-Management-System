package com.abhinav.lms.enrollment.service;

import com.abhinav.lms.common.dto.PagedResponse;
import com.abhinav.lms.enrollment.dto.EnrollmentResponse;
import com.abhinav.lms.security.model.UserPrincipal;

import java.util.UUID;

public interface EnrollmentService {

    EnrollmentResponse enrollCourse(UUID courseId, UserPrincipal currentUser);

    EnrollmentResponse dropCourse(UUID courseId, UserPrincipal currentUser);

    PagedResponse<EnrollmentResponse> getStudentEnrollments(UserPrincipal currentUser, int page, int size);

    PagedResponse<EnrollmentResponse> getCourseEnrollments(UUID courseId, UserPrincipal currentUser, int page, int size);
}
