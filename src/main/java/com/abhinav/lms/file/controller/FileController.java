package com.abhinav.lms.file.controller;

import com.abhinav.lms.common.constants.AppConstants;
import com.abhinav.lms.common.dto.ApiResponse;
import com.abhinav.lms.exception.BusinessException;
import com.abhinav.lms.file.dto.UploadResponse;
import com.abhinav.lms.file.service.StorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;

@RestController
@RequestMapping(AppConstants.API_V1 + "/files")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "File Storage Management", description = "Endpoints for uploading and downloading binary attachments")
public class FileController {

    private final StorageService storageService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Upload a profile photo, thumbnail, or assignment submission attachment (Authenticated users only)")
    public ApiResponse<UploadResponse> uploadFile(@RequestParam("file") MultipartFile file) {
        log.info("Request to upload file: {}", file.getOriginalFilename());
        String fileName = storageService.store(file);

        String fileUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path(AppConstants.API_V1)
                .path("/files/")
                .path(fileName)
                .toUriString();

        UploadResponse response = UploadResponse.builder()
                .fileName(fileName)
                .fileUrl(fileUrl)
                .contentType(file.getContentType())
                .size(file.getSize())
                .build();

        return ApiResponse.success(response, "File uploaded successfully");
    }

    @GetMapping("/{filename:.+}")
    @Operation(summary = "Download/serve a binary file statically (Public)")
    public ResponseEntity<Resource> serveFile(@PathVariable String filename, HttpServletRequest request) {
        log.debug("Request to serve file: {}", filename);
        Resource file = storageService.loadAsResource(filename);

        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(file.getFile().getAbsolutePath());
        } catch (IOException ex) {
            log.warn("Could not determine file type content mapping for {}", filename);
        }

        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getFilename() + "\"")
                .body(file);
    }
}
