package com.abhinav.lms.assignment.mapper;

import com.abhinav.lms.assignment.dto.AssignmentRequest;
import com.abhinav.lms.assignment.dto.AssignmentResponse;
import com.abhinav.lms.assignment.dto.SubmissionResponse;
import com.abhinav.lms.assignment.entity.Assignment;
import com.abhinav.lms.assignment.entity.AssignmentSubmission;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(
        componentModel = "spring",
        builder = @Builder(disableBuilder = true),
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface AssignmentMapper {

    @Mapping(target = "courseId", source = "course.id")
    @Mapping(target = "lessonId", source = "lesson.id")
    AssignmentResponse toResponse(Assignment assignment);

    List<AssignmentResponse> toResponseList(List<Assignment> assignments);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "course", ignore = true)
    @Mapping(target = "lesson", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    Assignment toEntity(AssignmentRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "course", ignore = true)
    @Mapping(target = "lesson", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    void updateEntityFromRequest(AssignmentRequest request, @MappingTarget Assignment assignment);

    @Mapping(target = "assignmentId", source = "assignment.id")
    @Mapping(target = "studentId", source = "student.id")
    @Mapping(target = "studentName", expression = "java(submission.getStudent().getFirstName() + \" \" + submission.getStudent().getLastName())")
    @Mapping(target = "gradedByName", expression = "java(submission.getGradedBy() == null ? null : submission.getGradedBy().getFirstName() + \" \" + submission.getGradedBy().getLastName())")
    SubmissionResponse toResponse(AssignmentSubmission submission);

    List<SubmissionResponse> toSubmissionResponseList(List<AssignmentSubmission> submissions);
}
