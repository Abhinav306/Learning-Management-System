package com.abhinav.lms.ai.recommendation.service;

import com.abhinav.lms.ai.recommendation.dto.RecommendationResponse;
import com.abhinav.lms.course.entity.Course;
import com.abhinav.lms.course.entity.CourseStatus;
import com.abhinav.lms.course.entity.DifficultyLevel;
import com.abhinav.lms.course.repository.CourseRepository;
import com.abhinav.lms.enrollment.entity.Enrollment;
import com.abhinav.lms.enrollment.repository.EnrollmentRepository;
import com.abhinav.lms.security.model.UserPrincipal;
import com.abhinav.lms.user.entity.User;
import com.abhinav.lms.user.entity.UserRole;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceImplTest {

    @Mock
    private ChatModel chatModel;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private RecommendationServiceImpl recommendationService;

    private User student;
    private UserPrincipal studentPrincipal;
    private Course course1;
    private Course course2;
    private Course course3;

    @BeforeEach
    void setUp() {
        student = User.builder()
                .id(UUID.randomUUID())
                .email("student@test.com")
                .role(UserRole.STUDENT)
                .build();

        studentPrincipal = UserPrincipal.create(student);

        course1 = Course.builder()
                .id(UUID.randomUUID())
                .title("Intro to Java")
                .difficulty(DifficultyLevel.BEGINNER)
                .status(CourseStatus.PUBLISHED)
                .build();

        course2 = Course.builder()
                .id(UUID.randomUUID())
                .title("Advanced Spring Boot")
                .difficulty(DifficultyLevel.ADVANCED)
                .status(CourseStatus.PUBLISHED)
                .build();

        course3 = Course.builder()
                .id(UUID.randomUUID())
                .title("Microservices Architecture")
                .difficulty(DifficultyLevel.ADVANCED)
                .status(CourseStatus.PUBLISHED)
                .build();
    }

    @Test
    void getPersonalizedRecommendations_ColdStart_ReturnsPopularCourses() {
        when(enrollmentRepository.findByStudentId(any(UUID.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        when(courseRepository.findPopularCourses(any(Pageable.class)))
                .thenReturn(List.of(course1, course2));

        List<RecommendationResponse> results = recommendationService.getPersonalizedRecommendations(studentPrincipal, 5);

        assertNotNull(results);
        assertEquals(2, results.size());
        assertEquals("Popular choice among students on our platform.", results.get(0).getReason());
    }

    @Test
    void getPersonalizedRecommendations_WithHistory_ReturnsPersonalized() {
        Enrollment enrollment = Enrollment.builder()
                .student(student)
                .course(course1)
                .build();

        when(enrollmentRepository.findByStudentId(any(UUID.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(enrollment)));

        when(courseRepository.findByStatus(any(CourseStatus.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(course1, course2, course3)));

        ChatResponse mockResponse = mock(ChatResponse.class);
        Generation mockGeneration = mock(Generation.class);
        AssistantMessage mockMessage = mock(AssistantMessage.class);

        when(mockResponse.getResult()).thenReturn(mockGeneration);
        when(mockGeneration.getOutput()).thenReturn(mockMessage);

        String jsonResponse = "[{" +
                "  \"courseId\": \"" + course2.getId() + "\"," +
                "  \"reason\": \"Matches interest in advanced java ecosystem.\"" +
                "}]";

        when(mockMessage.getText()).thenReturn(jsonResponse);
        when(chatModel.call(any(Prompt.class))).thenReturn(mockResponse);

        List<RecommendationResponse> results = recommendationService.getPersonalizedRecommendations(studentPrincipal, 5);

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(course2.getId(), results.get(0).getId());
        assertEquals("Matches interest in advanced java ecosystem.", results.get(0).getReason());
    }

    @Test
    void getPersonalizedRecommendations_LlmError_FallsBackToPopular() {
        Enrollment enrollment = Enrollment.builder()
                .student(student)
                .course(course1)
                .build();

        when(enrollmentRepository.findByStudentId(any(UUID.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(enrollment)));

        when(courseRepository.findByStatus(any(CourseStatus.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(course1, course2)));

        when(chatModel.call(any(Prompt.class))).thenThrow(new RuntimeException("LLM Timeout"));
        when(courseRepository.findPopularCourses(any(Pageable.class))).thenReturn(List.of(course2));

        List<RecommendationResponse> results = recommendationService.getPersonalizedRecommendations(studentPrincipal, 5);

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("Popular choice among students on our platform.", results.get(0).getReason());
    }
}
