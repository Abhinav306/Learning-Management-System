package com.abhinav.lms.course.repository;

import com.abhinav.lms.course.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, UUID> {

    List<Lesson> findBySectionIdOrderBySortOrderAsc(UUID sectionId);

    Optional<Lesson> findByIdAndSectionId(UUID id, UUID sectionId);

    long countBySectionCourseId(UUID courseId);

    List<Lesson> findBySectionCourseId(UUID courseId);
}
