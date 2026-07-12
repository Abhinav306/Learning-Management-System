package com.abhinav.lms.course.repository;

import com.abhinav.lms.course.entity.Section;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SectionRepository extends JpaRepository<Section, UUID> {

    List<Section> findByCourseIdOrderBySortOrderAsc(UUID courseId);

    Optional<Section> findByIdAndCourseId(UUID id, UUID courseId);
}
