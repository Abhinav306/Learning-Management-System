package com.abhinav.lms.course.service;

import com.abhinav.lms.course.dto.SectionRequest;
import com.abhinav.lms.course.dto.SectionResponse;
import com.abhinav.lms.security.model.UserPrincipal;

import java.util.List;
import java.util.UUID;

public interface SectionService {

    SectionResponse createSection(UUID courseId, SectionRequest request, UserPrincipal currentUser);

    SectionResponse getSectionById(UUID courseId, UUID sectionId);

    List<SectionResponse> getSectionsByCourse(UUID courseId);

    SectionResponse updateSection(UUID courseId, UUID sectionId, SectionRequest request, UserPrincipal currentUser);

    void deleteSection(UUID courseId, UUID sectionId, UserPrincipal currentUser);
}
