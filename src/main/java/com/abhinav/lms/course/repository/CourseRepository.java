package com.abhinav.lms.course.repository;

import com.abhinav.lms.course.entity.Course;
import com.abhinav.lms.course.entity.CourseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CourseRepository extends JpaRepository<Course, UUID>, JpaSpecificationExecutor<Course> {

    Page<Course> findByInstructorId(UUID instructorId, Pageable pageable);

    Page<Course> findByStatus(CourseStatus status, Pageable pageable);

    @Query("SELECT c FROM Course c " +
           "LEFT JOIN Enrollment e ON c.id = e.course.id " +
           "WHERE c.status = 'PUBLISHED' " +
           "GROUP BY c " +
           "ORDER BY COUNT(e) DESC")
    List<Course> findPopularCourses(Pageable pageable);
}
