package com.abhinav.lms.course.dto;

import com.abhinav.lms.course.entity.ContentType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonRequest {

    @NotBlank(message = "Lesson title is required")
    @Size(max = 255, message = "Lesson title must not exceed 255 characters")
    private String title;

    @Size(max = 4000, message = "Content must not exceed 4000 characters")
    private String content;

    @Size(max = 500, message = "Video URL must not exceed 500 characters")
    private String videoUrl;

    @Size(max = 500, message = "Resource URL must not exceed 500 characters")
    private String resourceUrl;

    @NotNull(message = "Duration is required")
    @Min(value = 0, message = "Duration must be at least 0 minutes")
    private Integer duration;

    @NotNull(message = "Sort order is required")
    @Min(value = 0, message = "Sort order must be at least 0")
    private Integer sortOrder;

    @NotNull(message = "Content type is required")
    private ContentType contentType;

    private boolean published;
}
