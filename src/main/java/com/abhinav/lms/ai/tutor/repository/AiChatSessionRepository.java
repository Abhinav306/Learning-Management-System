package com.abhinav.lms.ai.tutor.repository;

import com.abhinav.lms.ai.tutor.entity.AiChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AiChatSessionRepository extends JpaRepository<AiChatSession, UUID> {

    List<AiChatSession> findByUserIdOrderByUpdatedAtDesc(UUID userId);

    List<AiChatSession> findByUserIdAndCourseIdOrderByUpdatedAtDesc(UUID userId, UUID courseId);

    Optional<AiChatSession> findByIdAndUserId(UUID id, UUID userId);
}
