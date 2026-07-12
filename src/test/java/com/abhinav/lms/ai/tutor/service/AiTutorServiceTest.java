package com.abhinav.lms.ai.tutor.service;

import com.abhinav.lms.ai.tutor.dto.ChatSessionResponse;
import com.abhinav.lms.ai.tutor.dto.CreateSessionRequest;
import com.abhinav.lms.ai.tutor.entity.AiChatSession;
import com.abhinav.lms.ai.tutor.mapper.AiTutorMapper;
import com.abhinav.lms.ai.tutor.repository.AiChatMessageRepository;
import com.abhinav.lms.ai.tutor.repository.AiChatSessionRepository;
import com.abhinav.lms.course.repository.CourseRepository;
import com.abhinav.lms.security.model.UserPrincipal;
import com.abhinav.lms.user.entity.User;
import com.abhinav.lms.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.model.ChatModel;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiTutorServiceTest {

    @Mock
    private AiChatSessionRepository chatSessionRepository;

    @Mock
    private AiChatMessageRepository chatMessageRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private AiTutorMapper aiTutorMapper;

    @Mock
    private ChatModel chatModel;

    @InjectMocks
    private AiTutorServiceImpl aiTutorService;

    private UserPrincipal principal;
    private User user;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        principal = new UserPrincipal(userId, "student@lms.com", "password", null, null, true);
        user = User.builder().firstName("Student").lastName("User").email("student@lms.com").build();
    }

    @Test
    void createSession_Success() {
        CreateSessionRequest request = new CreateSessionRequest();
        request.setTitle("Physics Help");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(chatSessionRepository.save(any(AiChatSession.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(aiTutorMapper.toResponse(any(AiChatSession.class))).thenReturn(ChatSessionResponse.builder().title("Physics Help").build());

        ChatSessionResponse response = aiTutorService.createSession(request, principal);

        assertNotNull(response);
        assertEquals("Physics Help", response.getTitle());
        verify(chatSessionRepository, times(1)).save(any(AiChatSession.class));
    }
}
