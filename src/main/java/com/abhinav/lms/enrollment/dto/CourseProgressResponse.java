package com.abhinav.lms.enrollment.dto;

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
public class CourseProgressResponse {

    private Double progress;
    private long completedLessonsCount;
    private long totalLessonsCount;
    private List<UUID> completedLessonIds;
}
