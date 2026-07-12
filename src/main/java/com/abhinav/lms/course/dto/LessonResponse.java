package com.abhinav.lms.course.dto;

import com.abhinav.lms.course.entity.ContentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonResponse {

    private UUID id;
    private String title;
    private String content;
    private String videoUrl;
    private String resourceUrl;
    private Integer duration;
    private Integer sortOrder;
    private ContentType contentType;
    private boolean published;
    private UUID sectionId;
}
