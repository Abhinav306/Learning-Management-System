package com.abhinav.lms.course.service;

import com.abhinav.lms.common.dto.PagedResponse;
import com.abhinav.lms.course.dto.CourseRequest;
import com.abhinav.lms.course.dto.CourseResponse;
import com.abhinav.lms.course.entity.CourseStatus;
import com.abhinav.lms.course.entity.DifficultyLevel;
import com.abhinav.lms.security.model.UserPrincipal;

import java.util.UUID;

public interface CourseService {

    CourseResponse createCourse(CourseRequest request, UserPrincipal currentUser);

    CourseResponse getCourseById(UUID id);

    PagedResponse<CourseResponse> getAllCourses(int page, int size, String sortBy, String sortDir);

    PagedResponse<CourseResponse> searchCourses(String keyword, UUID categoryId, DifficultyLevel difficulty,
                                                CourseStatus status, UUID instructorId, int page, int size);

    CourseResponse updateCourse(UUID id, CourseRequest request, UserPrincipal currentUser);

    void deleteCourse(UUID id, UserPrincipal currentUser);
}
