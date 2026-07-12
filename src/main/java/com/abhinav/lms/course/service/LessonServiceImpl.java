package com.abhinav.lms.course.service;

import com.abhinav.lms.course.dto.LessonRequest;
import com.abhinav.lms.course.dto.LessonResponse;
import com.abhinav.lms.course.entity.Course;
import com.abhinav.lms.course.entity.Lesson;
import com.abhinav.lms.course.entity.Section;
import com.abhinav.lms.course.mapper.CourseMapper;
import com.abhinav.lms.course.repository.CourseRepository;
import com.abhinav.lms.course.repository.LessonRepository;
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
public class LessonServiceImpl implements LessonService {

    private final LessonRepository lessonRepository;
    private final SectionRepository sectionRepository;
    private final CourseRepository courseRepository;
    private final CourseMapper courseMapper;

    @Override
    @Transactional
    public LessonResponse createLesson(UUID courseId, UUID sectionId, LessonRequest request, UserPrincipal currentUser) {
        log.info("Creating lesson in section ID: {} under course ID: {}", sectionId, courseId);
        Course course = findCourseOrThrow(courseId);
        checkCourseOwnershipOrAdmin(course, currentUser);

        Section section = findSectionOrThrow(sectionId, courseId);

        Lesson lesson = courseMapper.toEntity(request);
        lesson.setSection(section);

        Lesson savedLesson = lessonRepository.save(lesson);
        log.info("Lesson created successfully with ID: {}", savedLesson.getId());
        return courseMapper.toResponse(savedLesson);
    }

    @Override
    public LessonResponse getLessonById(UUID courseId, UUID sectionId, UUID lessonId) {
        log.debug("Fetching lesson ID: {} in section ID: {} under course ID: {}", lessonId, sectionId, courseId);
        verifyCourseAndSectionMatch(courseId, sectionId);
        Lesson lesson = findLessonOrThrow(lessonId, sectionId);
        return courseMapper.toResponse(lesson);
    }

    @Override
    public List<LessonResponse> getLessonsBySection(UUID courseId, UUID sectionId) {
        log.debug("Fetching all lessons in section ID: {} under course ID: {}", sectionId, courseId);
        verifyCourseAndSectionMatch(courseId, sectionId);
        List<Lesson> lessons = lessonRepository.findBySectionIdOrderBySortOrderAsc(sectionId);
        return courseMapper.toLessonResponseList(lessons);
    }

    @Override
    @Transactional
    public LessonResponse updateLesson(UUID courseId, UUID sectionId, UUID lessonId, LessonRequest request, UserPrincipal currentUser) {
        log.info("Updating lesson ID: {} in section ID: {} under course ID: {}", lessonId, sectionId, courseId);
        Course course = findCourseOrThrow(courseId);
        checkCourseOwnershipOrAdmin(course, currentUser);

        verifyCourseAndSectionMatch(courseId, sectionId);
        Lesson lesson = findLessonOrThrow(lessonId, sectionId);

        courseMapper.updateEntityFromRequest(request, lesson);
        Lesson updatedLesson = lessonRepository.save(lesson);

        log.info("Lesson updated successfully with ID: {}", updatedLesson.getId());
        return courseMapper.toResponse(updatedLesson);
    }

    @Override
    @Transactional
    public void deleteLesson(UUID courseId, UUID sectionId, UUID lessonId, UserPrincipal currentUser) {
        log.info("Deleting lesson ID: {} in section ID: {} under course ID: {}", lessonId, sectionId, courseId);
        Course course = findCourseOrThrow(courseId);
        checkCourseOwnershipOrAdmin(course, currentUser);

        verifyCourseAndSectionMatch(courseId, sectionId);
        Lesson lesson = findLessonOrThrow(lessonId, sectionId);

        lessonRepository.delete(lesson);
        log.info("Lesson deleted successfully with ID: {}", lessonId);
    }

    @Override
    @Transactional
    public void reorderLessons(UUID courseId, UUID sectionId, List<UUID> lessonIds, UserPrincipal currentUser) {
        log.info("Reordering lessons in section ID: {} under course ID: {}", sectionId, courseId);
        Course course = findCourseOrThrow(courseId);
        checkCourseOwnershipOrAdmin(course, currentUser);

        verifyCourseAndSectionMatch(courseId, sectionId);

        for (int i = 0; i < lessonIds.size(); i++) {
            UUID lessonId = lessonIds.get(i);
            Lesson lesson = findLessonOrThrow(lessonId, sectionId);
            lesson.setSortOrder(i);
            lessonRepository.save(lesson);
        }
        log.info("Lessons reordered successfully in section ID: {}", sectionId);
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

    private Lesson findLessonOrThrow(UUID lessonId, UUID sectionId) {
        return lessonRepository.findByIdAndSectionId(lessonId, sectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson", "id", lessonId));
    }

    private void verifyCourseAndSectionMatch(UUID courseId, UUID sectionId) {
        if (!sectionRepository.existsById(sectionId)) {
            throw new ResourceNotFoundException("Section", "id", sectionId);
        }
        Section section = sectionRepository.findById(sectionId).orElse(null);
        if (section != null && !section.getCourse().getId().equals(courseId)) {
            throw new BusinessException("Section does not belong to the specified course");
        }
    }

    private void checkCourseOwnershipOrAdmin(Course course, UserPrincipal currentUser) {
        if (!currentUser.getRole().name().equals("ADMIN") &&
                !course.getInstructor().getId().equals(currentUser.getId())) {
            throw new BusinessException("You do not have permission to modify this course content");
        }
    }
}
