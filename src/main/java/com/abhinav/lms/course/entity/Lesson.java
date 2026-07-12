package com.abhinav.lms.course.entity;

import com.abhinav.lms.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
        name = "lessons",
        indexes = {
                @Index(name = "idx_lessons_section", columnList = "section_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class Lesson extends BaseEntity {

    @Column(nullable = false, length = 255)
    private String title;

    @Column(length = 4000)
    private String content;

    @Column(length = 500)
    private String videoUrl;

    @Column(length = 500)
    private String resourceUrl;

    @Column(nullable = false)
    @Builder.Default
    private Integer duration = 0; // in minutes

    @Column(nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ContentType contentType = ContentType.VIDEO;

    @Column(nullable = false)
    @Builder.Default
    private boolean published = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    private Section section;
}
