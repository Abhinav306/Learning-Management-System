package com.abhinav.lms.ai.service;

import com.abhinav.lms.ai.dto.RagQueryRequest;
import com.abhinav.lms.ai.dto.RagQueryResponse;
import com.abhinav.lms.security.model.UserPrincipal;
import com.abhinav.lms.user.entity.User;
import com.abhinav.lms.user.entity.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RagServiceTest {

    @Mock
    private ChatModel chatModel;

    @Mock
    private VectorStore vectorStore;

    @InjectMocks
    private RagServiceImpl ragService;

    private UserPrincipal studentPrincipal;

    @BeforeEach
    void setUp() {
        User student = User.builder()
                .id(UUID.randomUUID())
                .email("student@test.com")
                .role(UserRole.STUDENT)
                .build();
        studentPrincipal = UserPrincipal.create(student);
    }

    @Test
    void queryRag_Success() {
        RagQueryRequest request = RagQueryRequest.builder()
                .query("What is virtual memory?")
                .courseId(UUID.randomUUID())
                .build();

        org.springframework.ai.document.Document doc1 = new org.springframework.ai.document.Document(
                "Virtual memory is a memory management capability of an OS.",
                Map.of("filename", "os_notes.pdf", "courseId", request.getCourseId().toString())
        );

        when(vectorStore.similaritySearch(any(SearchRequest.class)))
                .thenReturn(List.of(doc1));

        ChatResponse mockResponse = mock(ChatResponse.class);
        Generation mockGeneration = mock(Generation.class);
        AssistantMessage mockMessage = mock(AssistantMessage.class);

        when(mockResponse.getResult()).thenReturn(mockGeneration);
        when(mockGeneration.getOutput()).thenReturn(mockMessage);
        when(mockMessage.getText()).thenReturn("Virtual memory is OS memory management.");
        when(chatModel.call(any(Prompt.class))).thenReturn(mockResponse);

        RagQueryResponse response = ragService.queryRag(request, studentPrincipal);

        assertNotNull(response);
        assertEquals("Virtual memory is OS memory management.", response.getAnswer());
        assertEquals(1, response.getSources().size());
        assertEquals("os_notes.pdf", response.getSources().get(0));
    }
}
