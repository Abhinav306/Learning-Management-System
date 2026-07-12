package com.abhinav.lms.enrollment.entity;

import com.abhinav.lms.common.entity.BaseEntity;
import com.abhinav.lms.course.entity.Lesson;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "lesson_progresses",
        uniqueConstraints = {
                @UniqueConstraint(name = "uc_enrollment_lesson", columnNames = {"enrollment_id", "lesson_id"})
        },
        indexes = {
                @Index(name = "idx_lesson_progresses_enrollment", columnList = "enrollment_id"),
                @Index(name = "idx_lesson_progresses_lesson", columnList = "lesson_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class LessonProgress extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrollment_id", nullable = false)
    private Enrollment enrollment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @Column(nullable = false)
    @Builder.Default
    private boolean completed = false;

    @Column
    private LocalDateTime completedAt;
}
