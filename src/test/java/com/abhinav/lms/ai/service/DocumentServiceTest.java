package com.abhinav.lms.ai.service;

import com.abhinav.lms.ai.dto.DocumentResponse;
import com.abhinav.lms.ai.entity.Document;
import com.abhinav.lms.ai.repository.DocumentRepository;
import com.abhinav.lms.course.entity.Course;
import com.abhinav.lms.course.repository.CourseRepository;
import com.abhinav.lms.security.model.UserPrincipal;
import com.abhinav.lms.user.entity.User;
import com.abhinav.lms.user.entity.UserRole;
import com.abhinav.lms.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private VectorStore vectorStore;

    @InjectMocks
    private DocumentServiceImpl documentService;

    private User instructor;
    private UserPrincipal instructorPrincipal;
    private Course course;
    private UUID courseId;

    @BeforeEach
    void setUp() {
        courseId = UUID.randomUUID();

        instructor = User.builder()
                .id(UUID.randomUUID())
                .email("instructor@test.com")
                .role(UserRole.INSTRUCTOR)
                .build();

        instructorPrincipal = UserPrincipal.create(instructor);

        course = Course.builder()
                .id(courseId)
                .title("Cloud Computing")
                .instructor(instructor)
                .build();
    }

    @Test
    void uploadDocument_Success() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "syllabus.txt",
                "text/plain",
                "This is the cloud computing syllabus. Topics include Virtualization, Containers, and Kubernetes.".getBytes()
        );

        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(instructor));
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));

        Document savedDoc = Document.builder()
                .id(UUID.randomUUID())
                .filename("syllabus.txt")
                .contentType("text/plain")
                .size(file.getSize())
                .chunkCount(1)
                .build();

        when(documentRepository.save(any(Document.class))).thenReturn(savedDoc);

        DocumentResponse response = documentService.uploadDocument(file, courseId, instructorPrincipal);

        assertNotNull(response);
        assertEquals("syllabus.txt", response.getFilename());
        verify(vectorStore, times(1)).accept(any());
        verify(documentRepository, times(1)).save(any(Document.class));
    }

    @Test
    void deleteDocument_Success() {
        UUID docId = UUID.randomUUID();
        Document document = Document.builder()
                .id(docId)
                .filename("test.txt")
                .chunkCount(2)
                .course(course)
                .user(instructor)
                .build();

        when(documentRepository.findById(docId)).thenReturn(Optional.of(document));

        documentService.deleteDocument(docId, instructorPrincipal);

        verify(vectorStore, times(1)).delete(List.of(docId + "_0", docId + "_1"));
        verify(documentRepository, times(1)).delete(document);
    }
}
