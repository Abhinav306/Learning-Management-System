package com.abhinav.lms.quiz.mapper;

import com.abhinav.lms.quiz.dto.AttemptResultResponse;
import com.abhinav.lms.quiz.dto.QuestionRequest;
import com.abhinav.lms.quiz.dto.QuestionResponse;
import com.abhinav.lms.quiz.dto.QuizRequest;
import com.abhinav.lms.quiz.dto.QuizResponse;
import com.abhinav.lms.quiz.entity.Quiz;
import com.abhinav.lms.quiz.entity.QuizAttempt;
import com.abhinav.lms.quiz.entity.QuizQuestion;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(
        componentModel = "spring",
        builder = @Builder(disableBuilder = true),
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface QuizMapper {

    @Mapping(target = "courseId", source = "course.id")
    @Mapping(target = "lessonId", source = "lesson.id")
    QuizResponse toResponse(Quiz quiz);

    List<QuizResponse> toResponseList(List<Quiz> quizzes);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "course", ignore = true)
    @Mapping(target = "lesson", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    Quiz toEntity(QuizRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "course", ignore = true)
    @Mapping(target = "lesson", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    void updateEntityFromRequest(QuizRequest request, @MappingTarget Quiz quiz);

    @Mapping(target = "quizId", source = "quiz.id")
    QuestionResponse toResponse(QuizQuestion question);

    List<QuestionResponse> toQuestionResponseList(List<QuizQuestion> questions);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "quiz", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    QuizQuestion toEntity(QuestionRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "quiz", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    void updateEntityFromRequest(QuestionRequest request, @MappingTarget QuizQuestion question);

    @Mapping(target = "attemptId", source = "id")
    @Mapping(target = "quizId", source = "quiz.id")
    @Mapping(target = "scorePercentage", expression = "java(attempt.getTotalPoints() == null || attempt.getTotalPoints() == 0 ? 0.0 : Math.round((attempt.getScore() / attempt.getTotalPoints() * 100.0) * 100.0) / 100.0)")
    AttemptResultResponse toResponse(QuizAttempt attempt);

    List<AttemptResultResponse> toAttemptResponseList(List<QuizAttempt> attempts);
}
