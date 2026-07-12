package com.abhinav.lms.ai.recommendation.controller;

import com.abhinav.lms.ai.recommendation.dto.RecommendationResponse;
import com.abhinav.lms.ai.recommendation.service.RecommendationService;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = RecommendationController.class)
@AutoConfigureMockMvc(addFilters = false) // Bypass JWT authentication filter for REST mappings verification
class RecommendationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RecommendationService recommendationService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtProperties jwtProperties;

    @Test
    @WithMockUser(roles = "STUDENT")
    void getRecommendations_Success() throws Exception {
        RecommendationResponse response = RecommendationResponse.builder()
                .id(UUID.randomUUID())
                .title("Spring Boot Core")
                .price(BigDecimal.valueOf(49.99))
                .reason("Personalized match")
                .build();

        when(recommendationService.getPersonalizedRecommendations(any(), anyInt()))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/ai/recommendations")
                        .param("limit", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].title").value("Spring Boot Core"))
                .andExpect(jsonPath("$.data[0].reason").value("Personalized match"));
    }

    @Test
    void getPopularCourses_Success() throws Exception {
        RecommendationResponse response = RecommendationResponse.builder()
                .id(UUID.randomUUID())
                .title("Popular Python")
                .reason("Popular choice")
                .build();

        when(recommendationService.getPopularCourses(anyInt()))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/ai/recommendations/popular")
                        .param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].title").value("Popular Python"));
    }
}
