package com.abhinav.lms.course.dto;

import jakarta.validation.constraints.NotEmpty;
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
public class LessonReorderRequest {

    @NotEmpty(message = "Lesson IDs list must not be empty")
    private List<UUID> lessonIds;
}
