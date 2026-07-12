package com.abhinav.lms.enrollment.service;

import com.abhinav.lms.course.entity.Lesson;
import com.abhinav.lms.course.repository.LessonRepository;
import com.abhinav.lms.enrollment.dto.CourseProgressResponse;
import com.abhinav.lms.enrollment.entity.Enrollment;
import com.abhinav.lms.enrollment.entity.EnrollmentStatus;
import com.abhinav.lms.enrollment.entity.LessonProgress;
import com.abhinav.lms.enrollment.repository.EnrollmentRepository;
import com.abhinav.lms.enrollment.repository.LessonProgressRepository;
import com.abhinav.lms.exception.BusinessException;
import com.abhinav.lms.exception.ResourceNotFoundException;
import com.abhinav.lms.security.model.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class LessonProgressServiceImpl implements LessonProgressService {

    private final LessonProgressRepository lessonProgressRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final LessonRepository lessonRepository;

    @Override
    @Transactional
    public CourseProgressResponse markLessonComplete(UUID enrollmentId, UUID lessonId, UserPrincipal currentUser) {
        log.info("Student ID: {} marking lesson ID: {} as complete in enrollment ID: {}", currentUser.getId(), lessonId, enrollmentId);
        Enrollment enrollment = getEnrollmentAndValidateOwner(enrollmentId, currentUser);

        if (enrollment.getStatus() == EnrollmentStatus.DROPPED || enrollment.getStatus() == EnrollmentStatus.EXPIRED) {
            throw new BusinessException("Cannot update progress on an inactive or dropped enrollment");
        }

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson", "id", lessonId));

        if (!lesson.getSection().getCourse().getId().equals(enrollment.getCourse().getId())) {
            throw new BusinessException("Lesson does not belong to the enrolled course");
        }

        Optional<LessonProgress> progressOpt = lessonProgressRepository.findByEnrollmentIdAndLessonId(enrollmentId, lessonId);
        if (progressOpt.isPresent()) {
            LessonProgress progress = progressOpt.get();
            if (progress.isCompleted()) {
                // Already complete, just return current status
                return getCourseProgress(enrollmentId, currentUser);
            }
            progress.setCompleted(true);
            progress.setCompletedAt(LocalDateTime.now());
            lessonProgressRepository.save(progress);
        } else {
            LessonProgress progress = LessonProgress.builder()
                    .enrollment(enrollment)
                    .lesson(lesson)
                    .completed(true)
                    .completedAt(LocalDateTime.now())
                    .build();
            lessonProgressRepository.save(progress);
        }

        return calculateAndSaveProgress(enrollment);
    }

    @Override
    @Transactional
    public CourseProgressResponse markLessonIncomplete(UUID enrollmentId, UUID lessonId, UserPrincipal currentUser) {
        log.info("Student ID: {} marking lesson ID: {} as incomplete in enrollment ID: {}", currentUser.getId(), lessonId, enrollmentId);
        Enrollment enrollment = getEnrollmentAndValidateOwner(enrollmentId, currentUser);

        if (enrollment.getStatus() == EnrollmentStatus.DROPPED || enrollment.getStatus() == EnrollmentStatus.EXPIRED) {
            throw new BusinessException("Cannot update progress on an inactive or dropped enrollment");
        }

        LessonProgress progress = lessonProgressRepository.findByEnrollmentIdAndLessonId(enrollmentId, lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("LessonProgress", "lessonId", lessonId));

        if (!progress.isCompleted()) {
            return getCourseProgress(enrollmentId, currentUser);
        }

        progress.setCompleted(false);
        progress.setCompletedAt(null);
        lessonProgressRepository.save(progress);

        return calculateAndSaveProgress(enrollment);
    }

    @Override
    public CourseProgressResponse getCourseProgress(UUID enrollmentId, UserPrincipal currentUser) {
        log.debug("Fetching course progress for enrollment ID: {}", enrollmentId);
        Enrollment enrollment = getEnrollmentAndValidateOwner(enrollmentId, currentUser);

        UUID courseId = enrollment.getCourse().getId();
        long totalLessons = lessonRepository.countBySectionCourseId(courseId);
        long completedLessons = lessonProgressRepository.countByEnrollmentIdAndCompletedTrue(enrollmentId);

        List<UUID> completedLessonIds = lessonProgressRepository.findByEnrollmentIdAndCompletedTrue(enrollmentId)
                .stream()
                .map(lp -> lp.getLesson().getId())
                .toList();

        return CourseProgressResponse.builder()
                .progress(enrollment.getProgress())
                .completedLessonsCount(completedLessons)
                .totalLessonsCount(totalLessons)
                .completedLessonIds(completedLessonIds)
                .build();
    }

    // ═══════════════════════ Private Helpers ═══════════════════════

    private Enrollment getEnrollmentAndValidateOwner(UUID enrollmentId, UserPrincipal currentUser) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment", "id", enrollmentId));

        if (!currentUser.getRole().name().equals("ADMIN") &&
                !enrollment.getStudent().getId().equals(currentUser.getId())) {
            throw new BusinessException("You do not have permission to view or modify this enrollment progress");
        }
        return enrollment;
    }

    private CourseProgressResponse calculateAndSaveProgress(Enrollment enrollment) {
        UUID courseId = enrollment.getCourse().getId();
        long totalLessons = lessonRepository.countBySectionCourseId(courseId);
        long completedLessons = lessonProgressRepository.countByEnrollmentIdAndCompletedTrue(enrollment.getId());

        double progressPercent = 0.0;
        if (totalLessons > 0) {
            progressPercent = ((double) completedLessons / totalLessons) * 100.0;
            progressPercent = Math.round(progressPercent * 100.0) / 100.0;
        }

        enrollment.setProgress(progressPercent);

        if (progressPercent >= 100.0 && totalLessons > 0) {
            enrollment.setStatus(EnrollmentStatus.COMPLETED);
            enrollment.setCompletedAt(LocalDateTime.now());
        } else {
            enrollment.setStatus(EnrollmentStatus.ACTIVE);
            enrollment.setCompletedAt(null);
        }

        enrollmentRepository.save(enrollment);

        List<UUID> completedLessonIds = lessonProgressRepository.findByEnrollmentIdAndCompletedTrue(enrollment.getId())
                .stream()
                .map(lp -> lp.getLesson().getId())
                .toList();

        return CourseProgressResponse.builder()
                .progress(progressPercent)
                .completedLessonsCount(completedLessons)
                .totalLessonsCount(totalLessons)
                .completedLessonIds(completedLessonIds)
                .build();
    }
}
