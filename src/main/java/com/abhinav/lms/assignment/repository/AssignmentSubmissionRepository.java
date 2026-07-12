package com.abhinav.lms.assignment.repository;

import com.abhinav.lms.assignment.entity.AssignmentSubmission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AssignmentSubmissionRepository extends JpaRepository<AssignmentSubmission, UUID> {

    Optional<AssignmentSubmission> findByAssignmentIdAndStudentId(UUID assignmentId, UUID studentId);

    Page<AssignmentSubmission> findByAssignmentId(UUID assignmentId, Pageable pageable);

    boolean existsByAssignmentIdAndStudentId(UUID assignmentId, UUID studentId);
}
