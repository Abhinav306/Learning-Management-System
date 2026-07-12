package com.abhinav.lms.course.entity;

import com.abhinav.lms.category.entity.Category;
import com.abhinav.lms.common.entity.BaseEntity;
import com.abhinav.lms.user.entity.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "courses",
        indexes = {
                @Index(name = "idx_courses_instructor", columnList = "instructor_id"),
                @Index(name = "idx_courses_category", columnList = "category_id"),
                @Index(name = "idx_courses_status", columnList = "status")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true, exclude = {"sections"})
public class Course extends BaseEntity {

    @Column(nullable = false, length = 255)
    private String title;

    @Column(length = 500)
    private String shortDescription;

    @Column(length = 4000)
    private String description;

    @Column(length = 500)
    private String thumbnailUrl;

    @Column(nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal price = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private DifficultyLevel difficulty = DifficultyLevel.ALL_LEVELS;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private CourseStatus status = CourseStatus.DRAFT;

    @Column(length = 50)
    @Builder.Default
    private String language = "English";

    @Column
    @Builder.Default
    private Double duration = 0.0; // Hours

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instructor_id", nullable = false)
    private User instructor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("sortOrder ASC")
    @Builder.Default
    private List<Section> sections = new ArrayList<>();
}
