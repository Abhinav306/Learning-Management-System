package com.abhinav.lms.ai.tutor.controller;

import com.abhinav.lms.ai.tutor.dto.ChatSessionResponse;
import com.abhinav.lms.ai.tutor.dto.CreateSessionRequest;
import com.abhinav.lms.ai.tutor.service.AiTutorService;
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
import reactor.core.publisher.Flux;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AiTutorController.class)
@AutoConfigureMockMvc(addFilters = false) // Bypass JWT authentication filters for controller mapping validation
class AiTutorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AiTutorService aiTutorService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtProperties jwtProperties;

    @Test
    @WithMockUser
    void createSession_Success() throws Exception {
        CreateSessionRequest request = new CreateSessionRequest();
        request.setTitle("Physics Chat");

        ChatSessionResponse response = ChatSessionResponse.builder()
                .id(UUID.randomUUID())
                .title("Physics Chat")
                .active(true)
                .build();

        when(aiTutorService.createSession(any(CreateSessionRequest.class), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/ai/tutor/sessions")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Physics Chat"));
    }

    @Test
    @WithMockUser
    void getUserSessions_Success() throws Exception {
        ChatSessionResponse response = ChatSessionResponse.builder()
                .id(UUID.randomUUID())
                .title("Physics Chat")
                .active(true)
                .build();

        when(aiTutorService.getUserSessions(any())).thenReturn(Collections.singletonList(response));

        mockMvc.perform(get("/api/v1/ai/tutor/sessions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].title").value("Physics Chat"));
    }

    @Test
    @WithMockUser
    void sendMessage_Success() throws Exception {
        UUID sessionId = UUID.randomUUID();
        when(aiTutorService.sendMessage(any(), any(), any())).thenReturn(Flux.just("Hello", " student", "!"));

        mockMvc.perform(post("/api/v1/ai/tutor/sessions/" + sessionId + "/messages")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\": \"Hi AI\"}"))
                .andExpect(status().isOk());
    }
}
