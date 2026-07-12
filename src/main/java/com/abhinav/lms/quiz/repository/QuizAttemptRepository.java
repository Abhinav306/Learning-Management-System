package com.abhinav.lms.quiz.repository;

import com.abhinav.lms.quiz.entity.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, UUID> {

    List<QuizAttempt> findByQuizIdAndStudentIdOrderByStartedAtDesc(UUID quizId, UUID studentId);

    long countByQuizIdAndStudentId(UUID quizId, UUID studentId);

    Optional<QuizAttempt> findByIdAndStudentId(UUID id, UUID studentId);
}
