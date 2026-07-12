package com.abhinav.lms.assignment.entity;

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
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "assignments",
        indexes = {
                @Index(name = "idx_assignments_course", columnList = "course_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class Assignment extends BaseEntity {

    @Column(nullable = false, length = 255)
    private String title;

    @Column(length = 4000)
    private String description;

    @Column(length = 4000)
    private String instructions;

    @Column(nullable = false)
    private Integer maxScore;

    @Column(nullable = false)
    private LocalDateTime dueDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id")
    private Lesson lesson;
}
