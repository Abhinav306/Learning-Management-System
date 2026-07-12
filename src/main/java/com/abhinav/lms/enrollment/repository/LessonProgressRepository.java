package com.abhinav.lms.enrollment.repository;

import com.abhinav.lms.enrollment.entity.LessonProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LessonProgressRepository extends JpaRepository<LessonProgress, UUID> {

    Optional<LessonProgress> findByEnrollmentIdAndLessonId(UUID enrollmentId, UUID lessonId);

    List<LessonProgress> findByEnrollmentIdAndCompletedTrue(UUID enrollmentId);

    long countByEnrollmentIdAndCompletedTrue(UUID enrollmentId);

    boolean existsByEnrollmentIdAndLessonIdAndCompletedTrue(UUID enrollmentId, UUID lessonId);
}
