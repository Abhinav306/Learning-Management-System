package com.abhinav.lms.course.service;

import com.abhinav.lms.course.dto.LessonRequest;
import com.abhinav.lms.course.dto.LessonResponse;
import com.abhinav.lms.security.model.UserPrincipal;

import java.util.List;
import java.util.UUID;

public interface LessonService {

    LessonResponse createLesson(UUID courseId, UUID sectionId, LessonRequest request, UserPrincipal currentUser);

    LessonResponse getLessonById(UUID courseId, UUID sectionId, UUID lessonId);

    List<LessonResponse> getLessonsBySection(UUID courseId, UUID sectionId);

    LessonResponse updateLesson(UUID courseId, UUID sectionId, UUID lessonId, LessonRequest request, UserPrincipal currentUser);

    void deleteLesson(UUID courseId, UUID sectionId, UUID lessonId, UserPrincipal currentUser);

    void reorderLessons(UUID courseId, UUID sectionId, List<UUID> lessonIds, UserPrincipal currentUser);
}
