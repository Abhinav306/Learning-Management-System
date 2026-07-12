package com.abhinav.lms.ai.tutor.service;

import com.abhinav.lms.ai.tutor.dto.ChatMessageResponse;
import com.abhinav.lms.ai.tutor.dto.ChatSessionResponse;
import com.abhinav.lms.ai.tutor.dto.CreateSessionRequest;
import com.abhinav.lms.ai.tutor.dto.SendMessageRequest;
import com.abhinav.lms.security.model.UserPrincipal;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.UUID;

public interface AiTutorService {

    ChatSessionResponse createSession(CreateSessionRequest request, UserPrincipal principal);

    List<ChatSessionResponse> getUserSessions(UserPrincipal principal);

    List<ChatSessionResponse> getUserSessionsByCourse(UUID courseId, UserPrincipal principal);

    ChatSessionResponse getSession(UUID sessionId, UserPrincipal principal);

    List<ChatMessageResponse> getSessionMessages(UUID sessionId, UserPrincipal principal);

    Flux<String> sendMessage(UUID sessionId, SendMessageRequest request, UserPrincipal principal);

    void deleteSession(UUID sessionId, UserPrincipal principal);
}
