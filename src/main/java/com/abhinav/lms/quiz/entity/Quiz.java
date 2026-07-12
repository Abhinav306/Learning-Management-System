package com.abhinav.lms.quiz.entity;

import com.abhinav.lms.common.entity.BaseEntity;
import com.abhinav.lms.course.entity.Course;
import com.abhinav.lms.course.entity.Lesson;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(
        name = "quizzes",
        indexes = {
                @Index(name = "idx_quizzes_course", columnList = "course_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class Quiz extends BaseEntity {

    @Column(nullable = false, length = 255)
    private String title;

    @Column(length = 4000)
    private String description;

    @Column
    private Integer timeLimit; // in minutes

    @Column(nullable = false)
    @Builder.Default
    private Integer passingScore = 60; // passing percentage, e.g. 60

    @Column(nullable = false)
    @Builder.Default
    private Integer maxAttempts = 3;

    @Column(nullable = false)
    @Builder.Default
    private boolean shuffleQuestions = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id")
    private Lesson lesson;

    @Column(nullable = false)
    @Builder.Default
    private boolean aiGenerated = false;
}
