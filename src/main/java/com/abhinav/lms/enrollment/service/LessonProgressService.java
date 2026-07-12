package com.abhinav.lms.enrollment.service;

import com.abhinav.lms.enrollment.dto.CourseProgressResponse;
import com.abhinav.lms.security.model.UserPrincipal;

import java.util.UUID;

public interface LessonProgressService {

    CourseProgressResponse markLessonComplete(UUID enrollmentId, UUID lessonId, UserPrincipal currentUser);

    CourseProgressResponse markLessonIncomplete(UUID enrollmentId, UUID lessonId, UserPrincipal currentUser);

    CourseProgressResponse getCourseProgress(UUID enrollmentId, UserPrincipal currentUser);
}
