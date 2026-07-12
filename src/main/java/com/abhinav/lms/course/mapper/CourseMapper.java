package com.abhinav.lms.course.mapper;

import com.abhinav.lms.category.mapper.CategoryMapper;
import com.abhinav.lms.course.dto.CourseRequest;
import com.abhinav.lms.course.dto.CourseResponse;
import com.abhinav.lms.course.dto.SectionRequest;
import com.abhinav.lms.course.dto.SectionResponse;
import com.abhinav.lms.course.dto.LessonRequest;
import com.abhinav.lms.course.dto.LessonResponse;
import com.abhinav.lms.course.entity.Course;
import com.abhinav.lms.course.entity.Lesson;
import com.abhinav.lms.course.entity.Section;
import com.abhinav.lms.user.mapper.UserMapper;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(
        componentModel = "spring",
        uses = {UserMapper.class, CategoryMapper.class},
        builder = @Builder(disableBuilder = true),
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface CourseMapper {

    CourseResponse toResponse(Course course);

    List<CourseResponse> toResponseList(List<Course> courses);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "instructor", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "sections", ignore = true)
    @Mapping(target = "duration", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    Course toEntity(CourseRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "instructor", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "sections", ignore = true)
    @Mapping(target = "duration", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    void updateEntityFromRequest(CourseRequest request, @MappingTarget Course course);

    @Mapping(target = "courseId", source = "course.id")
    SectionResponse toResponse(Section section);

    List<SectionResponse> toSectionResponseList(List<Section> sections);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "course", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    Section toEntity(SectionRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "course", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    void updateEntityFromRequest(SectionRequest request, @MappingTarget Section section);

    @Mapping(target = "sectionId", source = "section.id")
    LessonResponse toResponse(Lesson lesson);

    List<LessonResponse> toLessonResponseList(List<Lesson> lessons);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "section", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    Lesson toEntity(LessonRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "section", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    void updateEntityFromRequest(LessonRequest request, @MappingTarget Lesson lesson);
}
