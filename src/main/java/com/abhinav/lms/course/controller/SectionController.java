package com.abhinav.lms.course.controller;

import com.abhinav.lms.common.constants.AppConstants;
import com.abhinav.lms.common.dto.ApiResponse;
import com.abhinav.lms.course.dto.SectionRequest;
import com.abhinav.lms.course.dto.SectionResponse;
import com.abhinav.lms.course.service.SectionService;
import com.abhinav.lms.security.model.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(AppConstants.API_V1 + "/courses/{courseId}/sections")
@RequiredArgsConstructor
@Tag(name = "Course Section Management", description = "Endpoints for managing sections within a course (instructor owner writes, public reads)")
public class SectionController {

    private final SectionService sectionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @Operation(summary = "Create a new section in a course (Instructor owner / Admin only)")
    public ApiResponse<SectionResponse> createSection(
            @PathVariable UUID courseId,
            @Valid @RequestBody SectionRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        SectionResponse response = sectionService.createSection(courseId, request, principal);
        return ApiResponse.created(response, "Section created successfully");
    }

    @GetMapping("/{sectionId}")
    @Operation(summary = "Get section by ID (Public)")
    public ApiResponse<SectionResponse> getSectionById(
            @PathVariable UUID courseId,
            @PathVariable UUID sectionId) {
        SectionResponse response = sectionService.getSectionById(courseId, sectionId);
        return ApiResponse.success(response);
    }

    @GetMapping
    @Operation(summary = "Get all sections in a course, sorted (Public)")
    public ApiResponse<List<SectionResponse>> getSectionsByCourse(@PathVariable UUID courseId) {
        List<SectionResponse> response = sectionService.getSectionsByCourse(courseId);
        return ApiResponse.success(response);
    }

    @PutMapping("/{sectionId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @Operation(summary = "Update an existing section (Instructor owner / Admin only)")
    public ApiResponse<SectionResponse> updateSection(
            @PathVariable UUID courseId,
            @PathVariable UUID sectionId,
            @Valid @RequestBody SectionRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        SectionResponse response = sectionService.updateSection(courseId, sectionId, request, principal);
        return ApiResponse.success(response, "Section updated successfully");
    }

    @DeleteMapping("/{sectionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @Operation(summary = "Delete a section (Instructor owner / Admin only)")
    public void deleteSection(
            @PathVariable UUID courseId,
            @PathVariable UUID sectionId,
            @AuthenticationPrincipal UserPrincipal principal) {
        sectionService.deleteSection(courseId, sectionId, principal);
    }
}
