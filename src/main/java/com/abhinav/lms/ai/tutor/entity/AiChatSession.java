package com.abhinav.lms.ai.tutor.entity;

import com.abhinav.lms.common.entity.BaseEntity;
import com.abhinav.lms.course.entity.Course;
import com.abhinav.lms.user.entity.User;
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
        name = "ai_chat_sessions",
        indexes = {
                @Index(name = "idx_ai_sessions_user", columnList = "user_id"),
                @Index(name = "idx_ai_sessions_course", columnList = "course_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class AiChatSession extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false)
    @Builder.Default
    private int totalTokens = 0;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;
}
