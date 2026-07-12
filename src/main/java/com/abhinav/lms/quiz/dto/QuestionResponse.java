package com.abhinav.lms.quiz.dto;

import com.abhinav.lms.quiz.entity.QuestionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionResponse {

    private UUID id;
    private UUID quizId;
    private String questionText;
    private QuestionType type;
    private List<String> options;
    private String correctAnswer;
    private String explanation;
    private Integer points;
    private Integer sortOrder;
}
