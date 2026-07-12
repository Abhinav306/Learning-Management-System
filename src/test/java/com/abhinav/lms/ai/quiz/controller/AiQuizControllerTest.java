package com.abhinav.lms.ai.quiz.controller;

import com.abhinav.lms.ai.quiz.dto.AiQuizGenerationRequest;
import com.abhinav.lms.ai.quiz.dto.AiQuizGenerationResponse;
import com.abhinav.lms.ai.quiz.service.AiQuizService;
import com.abhinav.lms.quiz.dto.QuestionResponse;
import com.abhinav.lms.quiz.dto.QuizResponse;
import com.abhinav.lms.security.config.JwtProperties;
import com.abhinav.lms.security.filter.JwtAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AiQuizController.class)
@AutoConfigureMockMvc(addFilters = false) // Bypass security filters for API mappings checks
class AiQuizControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AiQuizService aiQuizService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtProperties jwtProperties;

    @Test
    @WithMockUser(roles = "INSTRUCTOR")
    void generateQuiz_Success() throws Exception {
        UUID courseId = UUID.randomUUID();
        UUID lessonId = UUID.randomUUID();

        AiQuizGenerationRequest request = AiQuizGenerationRequest.builder()
                .lessonId(lessonId)
                .numberOfQuestions(3)
                .build();

        QuizResponse quizResponse = QuizResponse.builder()
                .id(UUID.randomUUID())
                .title("AI Generated Quiz")
                .build();

        QuestionResponse questionResponse = QuestionResponse.builder()
                .id(UUID.randomUUID())
                .questionText("Sample Question?")
                .build();

        AiQuizGenerationResponse response = AiQuizGenerationResponse.builder()
                .quiz(quizResponse)
                .questions(List.of(questionResponse))
                .metadata(AiQuizGenerationResponse.AiMetadata.builder()
                        .model("gpt-4o-mini")
                        .totalTokens(100L)
                        .build())
                .build();

        when(aiQuizService.generateQuiz(any(UUID.class), any(AiQuizGenerationRequest.class), any()))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/courses/" + courseId + "/ai/quiz/generate")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.quiz.title").value("AI Generated Quiz"))
                .andExpect(jsonPath("$.data.questions[0].questionText").value("Sample Question?"));
    }
}
