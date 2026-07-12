package com.abhinav.lms.ai.controller;

import com.abhinav.lms.ai.dto.DocumentResponse;
import com.abhinav.lms.ai.dto.RagQueryRequest;
import com.abhinav.lms.ai.dto.RagQueryResponse;
import com.abhinav.lms.ai.service.DocumentService;
import com.abhinav.lms.ai.service.RagService;
import com.abhinav.lms.common.constants.AppConstants;
import com.abhinav.lms.common.dto.ApiResponse;
import com.abhinav.lms.security.model.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(AppConstants.API_V1 + "/ai")
@RequiredArgsConstructor
@Tag(name = "AI RAG Operations", description = "Endpoints for document ingestion, deletion, and context-aware RAG queries")
public class RagController {

    private final DocumentService documentService;
    private final RagService ragService;

    @PostMapping(value = "/documents/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    @Operation(summary = "Upload and process document (PDF/DOCX/TXT) for RAG (Instructors/Admins only)")
    public ApiResponse<DocumentResponse> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "courseId", required = false) UUID courseId,
            @AuthenticationPrincipal UserPrincipal principal) {
        DocumentResponse response = documentService.uploadDocument(file, courseId, principal);
        return ApiResponse.created(response, "Document uploaded and parsed successfully");
    }

    @GetMapping("/documents")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List documents (filter by courseId or fetch user-scoped documents)")
    public ApiResponse<List<DocumentResponse>> listDocuments(
            @RequestParam(value = "courseId", required = false) UUID courseId,
            @AuthenticationPrincipal UserPrincipal principal) {
        List<DocumentResponse> documents = documentService.listDocuments(courseId, principal);
        return ApiResponse.success(documents);
    }

    @DeleteMapping("/documents/{id}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    @Operation(summary = "Delete uploaded document and its vector store chunks (Instructors/Admins only)")
    public ApiResponse<Void> deleteDocument(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal) {
        documentService.deleteDocument(id, principal);
        return ApiResponse.success(null, "Document deleted successfully");
    }

    @PostMapping("/rag/query")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Perform RAG query over document context")
    public ApiResponse<RagQueryResponse> queryRag(
            @Valid @RequestBody RagQueryRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        RagQueryResponse response = ragService.queryRag(request, principal);
        return ApiResponse.success(response);
    }
}
