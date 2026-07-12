package com.abhinav.lms.quiz.entity;

import com.abhinav.lms.common.entity.BaseEntity;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "quiz_questions",
        indexes = {
                @Index(name = "idx_questions_quiz", columnList = "quiz_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class QuizQuestion extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @Column(nullable = false, length = 4000)
    private String questionText;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private QuestionType type;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "quiz_question_options",
            joinColumns = @JoinColumn(name = "question_id")
    )
    @Column(name = "option_text", length = 1000)
    @OrderColumn(name = "option_order")
    @Builder.Default
    private List<String> options = new ArrayList<>();

    @Column(nullable = false, length = 1000)
    private String correctAnswer;

    @Column(length = 1000)
    private String explanation;

    @Column(nullable = false)
    @Builder.Default
    private Integer points = 1;

    @Column(nullable = false)
    private Integer sortOrder;
}
