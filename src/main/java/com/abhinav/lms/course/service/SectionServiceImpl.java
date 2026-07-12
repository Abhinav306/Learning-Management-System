package com.abhinav.lms.course.service;

import com.abhinav.lms.course.dto.SectionRequest;
import com.abhinav.lms.course.dto.SectionResponse;
import com.abhinav.lms.course.entity.Course;
import com.abhinav.lms.course.entity.Section;
import com.abhinav.lms.course.mapper.CourseMapper;
import com.abhinav.lms.course.repository.CourseRepository;
import com.abhinav.lms.course.repository.SectionRepository;
import com.abhinav.lms.exception.BusinessException;
import com.abhinav.lms.exception.ResourceNotFoundException;
import com.abhinav.lms.security.model.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SectionServiceImpl implements SectionService {

    private final SectionRepository sectionRepository;
    private final CourseRepository courseRepository;
    private final CourseMapper courseMapper;

    @Override
    @Transactional
    public SectionResponse createSection(UUID courseId, SectionRequest request, UserPrincipal currentUser) {
        log.info("Creating section in course ID: {}", courseId);
        Course course = findCourseOrThrow(courseId);

        checkCourseOwnershipOrAdmin(course, currentUser);

        Section section = courseMapper.toEntity(request);
        section.setCourse(course);

        Section savedSection = sectionRepository.save(section);
        log.info("Section created successfully with ID: {}", savedSection.getId());
        return courseMapper.toResponse(savedSection);
    }

    @Override
    public SectionResponse getSectionById(UUID courseId, UUID sectionId) {
        log.debug("Fetching section ID: {} in course ID: {}", sectionId, courseId);
        Section section = findSectionOrThrow(sectionId, courseId);
        return courseMapper.toResponse(section);
    }

    @Override
    public List<SectionResponse> getSectionsByCourse(UUID courseId) {
        log.debug("Fetching all sections in course ID: {}", courseId);
        List<Section> sections = sectionRepository.findByCourseIdOrderBySortOrderAsc(courseId);
        return courseMapper.toSectionResponseList(sections);
    }

    @Override
    @Transactional
    public SectionResponse updateSection(UUID courseId, UUID sectionId, SectionRequest request, UserPrincipal currentUser) {
        log.info("Updating section ID: {} in course ID: {}", sectionId, courseId);
        Course course = findCourseOrThrow(courseId);

        checkCourseOwnershipOrAdmin(course, currentUser);

        Section section = findSectionOrThrow(sectionId, courseId);
        courseMapper.updateEntityFromRequest(request, section);

        Section updatedSection = sectionRepository.save(section);
        log.info("Section updated successfully with ID: {}", updatedSection.getId());
        return courseMapper.toResponse(updatedSection);
    }

    @Override
    @Transactional
    public void deleteSection(UUID courseId, UUID sectionId, UserPrincipal currentUser) {
        log.info("Deleting section ID: {} in course ID: {}", sectionId, courseId);
        Course course = findCourseOrThrow(courseId);

        checkCourseOwnershipOrAdmin(course, currentUser);

        Section section = findSectionOrThrow(sectionId, courseId);
        sectionRepository.delete(section);
        log.info("Section deleted successfully with ID: {}", sectionId);
    }

    // ═══════════════════════ Private Helpers ═══════════════════════

    private Course findCourseOrThrow(UUID courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));
    }

    private Section findSectionOrThrow(UUID sectionId, UUID courseId) {
        return sectionRepository.findByIdAndCourseId(sectionId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Section", "id", sectionId));
    }

    private void checkCourseOwnershipOrAdmin(Course course, UserPrincipal currentUser) {
        if (!currentUser.getRole().name().equals("ADMIN") &&
                !course.getInstructor().getId().equals(currentUser.getId())) {
            throw new BusinessException("You do not have permission to modify this course section");
        }
    }
}
