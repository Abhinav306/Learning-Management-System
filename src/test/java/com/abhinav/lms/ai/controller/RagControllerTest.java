package com.abhinav.lms.ai.controller;

import com.abhinav.lms.ai.dto.DocumentResponse;
import com.abhinav.lms.ai.dto.RagQueryRequest;
import com.abhinav.lms.ai.dto.RagQueryResponse;
import com.abhinav.lms.ai.service.DocumentService;
import com.abhinav.lms.ai.service.RagService;
import com.abhinav.lms.security.config.JwtProperties;
import com.abhinav.lms.security.filter.JwtAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = RagController.class)
@AutoConfigureMockMvc(addFilters = false) // Bypass JWT authentication filter for API routes checks
class RagControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DocumentService documentService;

    @MockBean
    private RagService ragService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtProperties jwtProperties;

    @Test
    @WithMockUser(roles = "INSTRUCTOR")
    void uploadDocument_Success() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "notes.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "LMS system documentation".getBytes()
        );

        UUID courseId = UUID.randomUUID();
        DocumentResponse response = DocumentResponse.builder()
                .id(UUID.randomUUID())
                .filename("notes.txt")
                .contentType("text/plain")
                .size(file.getSize())
                .chunkCount(1)
                .processedAt(LocalDateTime.now())
                .build();

        when(documentService.uploadDocument(any(), any(), any())).thenReturn(response);

        mockMvc.perform(multipart("/api/v1/ai/documents/upload")
                        .file(file)
                        .param("courseId", courseId.toString())
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.filename").value("notes.txt"));
    }

    @Test
    @WithMockUser
    void listDocuments_Success() throws Exception {
        UUID courseId = UUID.randomUUID();
        DocumentResponse doc = DocumentResponse.builder()
                .id(UUID.randomUUID())
                .filename("notes.txt")
                .build();

        when(documentService.listDocuments(any(), any())).thenReturn(List.of(doc));

        mockMvc.perform(get("/api/v1/ai/documents")
                        .param("courseId", courseId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].filename").value("notes.txt"));
    }

    @Test
    @WithMockUser(roles = "INSTRUCTOR")
    void deleteDocument_Success() throws Exception {
        UUID docId = UUID.randomUUID();
        doNothing().when(documentService).deleteDocument(any(), any());

        mockMvc.perform(delete("/api/v1/ai/documents/" + docId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser
    void queryRag_Success() throws Exception {
        RagQueryRequest request = RagQueryRequest.builder()
                .query("What is virtual memory?")
                .build();

        RagQueryResponse response = RagQueryResponse.builder()
                .answer("Virtual memory handles memory mapping.")
                .sources(List.of("os_notes.pdf"))
                .build();

        when(ragService.queryRag(any(), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/ai/rag/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.answer").value("Virtual memory handles memory mapping."))
                .andExpect(jsonPath("$.data.sources[0]").value("os_notes.pdf"));
    }
}
