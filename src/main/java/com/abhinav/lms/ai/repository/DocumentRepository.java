package com.abhinav.lms.ai.repository;

import com.abhinav.lms.ai.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DocumentRepository extends JpaRepository<Document, UUID> {
    List<Document> findByCourseId(UUID courseId);
    List<Document> findByUserId(UUID userId);
}
