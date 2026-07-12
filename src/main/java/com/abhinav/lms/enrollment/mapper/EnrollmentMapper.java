package com.abhinav.lms.enrollment.mapper;

import com.abhinav.lms.enrollment.dto.EnrollmentResponse;
import com.abhinav.lms.enrollment.entity.Enrollment;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface EnrollmentMapper {

    @Mapping(target = "studentId", source = "student.id")
    @Mapping(target = "studentName", expression = "java(enrollment.getStudent().getFirstName() + \" \" + enrollment.getStudent().getLastName())")
    @Mapping(target = "courseId", source = "course.id")
    @Mapping(target = "courseTitle", source = "course.title")
    EnrollmentResponse toResponse(Enrollment enrollment);

    List<EnrollmentResponse> toResponseList(List<Enrollment> enrollments);
}
