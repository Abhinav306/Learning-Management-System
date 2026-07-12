package com.abhinav.lms.ai.tutor.controller;

import com.abhinav.lms.ai.tutor.dto.ChatMessageResponse;
import com.abhinav.lms.ai.tutor.dto.ChatSessionResponse;
import com.abhinav.lms.ai.tutor.dto.CreateSessionRequest;
import com.abhinav.lms.ai.tutor.dto.SendMessageRequest;
import com.abhinav.lms.ai.tutor.service.AiTutorService;
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
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(AppConstants.API_V1 + "/ai/tutor")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@Tag(name = "AI Tutor", description = "Endpoints for AI tutoring chatbot chat sessions, message streaming, and history")
public class AiTutorController {

    private final AiTutorService aiTutorService;

    @PostMapping("/sessions")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new AI chat session, optionally linked to a specific course")
    public ApiResponse<ChatSessionResponse> createSession(
            @Valid @RequestBody CreateSessionRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        ChatSessionResponse response = aiTutorService.createSession(request, principal);
        return ApiResponse.created(response, "AI chat session created successfully");
    }

    @GetMapping("/sessions")
    @Operation(summary = "Get list of AI chat sessions for the current authenticated user")
    public ApiResponse<List<ChatSessionResponse>> getUserSessions(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) UUID courseId) {
        List<ChatSessionResponse> response;
        if (courseId != null) {
            response = aiTutorService.getUserSessionsByCourse(courseId, principal);
        } else {
            response = aiTutorService.getUserSessions(principal);
        }
        return ApiResponse.success(response);
    }

    @GetMapping("/sessions/{sessionId}")
    @Operation(summary = "Get single AI chat session details")
    public ApiResponse<ChatSessionResponse> getSession(
            @PathVariable UUID sessionId,
            @AuthenticationPrincipal UserPrincipal principal) {
        ChatSessionResponse response = aiTutorService.getSession(sessionId, principal);
        return ApiResponse.success(response);
    }

    @GetMapping("/sessions/{sessionId}/messages")
    @Operation(summary = "Get list of messages (conversation history) for a specific AI chat session")
    public ApiResponse<List<ChatMessageResponse>> getSessionMessages(
            @PathVariable UUID sessionId,
            @AuthenticationPrincipal UserPrincipal principal) {
        List<ChatMessageResponse> response = aiTutorService.getSessionMessages(sessionId, principal);
        return ApiResponse.success(response);
    }

    @PostMapping(value = "/sessions/{sessionId}/messages", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Send a message to an AI chat session and stream response via Server-Sent Events (SSE)")
    public Flux<String> sendMessage(
            @PathVariable UUID sessionId,
            @Valid @RequestBody SendMessageRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return aiTutorService.sendMessage(sessionId, request, principal);
    }

    @DeleteMapping("/sessions/{sessionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete an AI chat session and its message history")
    public void deleteSession(
            @PathVariable UUID sessionId,
            @AuthenticationPrincipal UserPrincipal principal) {
        aiTutorService.deleteSession(sessionId, principal);
    }
}
