package com.abhinav.lms.ai.service;

import com.abhinav.lms.ai.dto.DocumentResponse;
import com.abhinav.lms.security.model.UserPrincipal;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface DocumentService {
    DocumentResponse uploadDocument(MultipartFile file, UUID courseId, UserPrincipal user);
    List<DocumentResponse> listDocuments(UUID courseId, UserPrincipal user);
    void deleteDocument(UUID documentId, UserPrincipal user);
}
