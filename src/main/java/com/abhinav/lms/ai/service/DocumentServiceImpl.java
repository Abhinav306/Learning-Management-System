package com.abhinav.lms.ai.service;

import com.abhinav.lms.ai.dto.DocumentResponse;
import com.abhinav.lms.ai.entity.Document;
import com.abhinav.lms.ai.repository.DocumentRepository;
import com.abhinav.lms.course.entity.Course;
import com.abhinav.lms.course.repository.CourseRepository;
import com.abhinav.lms.exception.BusinessException;
import com.abhinav.lms.exception.ResourceNotFoundException;
import com.abhinav.lms.security.model.UserPrincipal;
import com.abhinav.lms.user.entity.User;
import com.abhinav.lms.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final VectorStore vectorStore;

    @Override
    @Transactional
    public DocumentResponse uploadDocument(MultipartFile file, UUID courseId, UserPrincipal currentUser) {
        log.info("Uploading document: {}, size: {}, courseId: {}", file.getOriginalFilename(), file.getSize(), courseId);

        // 1. Resolve User and Course
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUser.getId()));

        Course course = null;
        if (courseId != null) {
            course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));
            checkCoursePermission(course, currentUser);
        }

        // 2. Extract Text based on Content Type
        String text;
        String filename = file.getOriginalFilename();
        if (filename == null) {
            filename = "document_" + UUID.randomUUID() + ".txt";
        }

        try {
            if (filename.toLowerCase().endsWith(".pdf")) {
                text = extractTextFromPdf(file.getBytes());
            } else if (filename.toLowerCase().endsWith(".docx")) {
                text = extractTextFromDocx(file.getInputStream());
            } else {
                text = new String(file.getBytes(), StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            log.error("Failed to extract text from document: {}", filename, e);
            throw new BusinessException("Failed to read document content: " + e.getMessage());
        }

        if (text == null || text.trim().isEmpty()) {
            throw new BusinessException("Document does not contain any readable text.");
        }

        // 3. Persist Metadata in DB (Draft with chunk count = 0 initially)
        UUID documentId = UUID.randomUUID();
        Document document = Document.builder()
                .id(documentId)
                .user(user)
                .course(course)
                .filename(filename)
                .contentType(file.getContentType() != null ? file.getContentType() : "text/plain")
                .size(file.getSize())
                .chunkCount(0)
                .processedAt(LocalDateTime.now())
                .build();

        // 4. Split Text into Chunks and Generate Vector Store Embeddings
        TokenTextSplitter splitter = new TokenTextSplitter();
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("documentId", documentId.toString());
        metadata.put("filename", filename);
        if (courseId != null) {
            metadata.put("courseId", courseId.toString());
        }

        org.springframework.ai.document.Document docToSplit = new org.springframework.ai.document.Document(text, metadata);
        List<org.springframework.ai.document.Document> rawChunks = splitter.apply(List.of(docToSplit));

        // Format chunk IDs statelessly (documentId_index) to allow clean deletion
        List<org.springframework.ai.document.Document> processedChunks = new ArrayList<>();
        for (int i = 0; i < rawChunks.size(); i++) {
            org.springframework.ai.document.Document chunk = rawChunks.get(i);
            org.springframework.ai.document.Document newChunk = org.springframework.ai.document.Document.builder()
                    .id(documentId + "_" + i)
                    .text(chunk.getText())
                    .metadata(chunk.getMetadata())
                    .build();
            processedChunks.add(newChunk);
        }

        log.info("Sending {} chunks to vector store...", processedChunks.size());
        vectorStore.accept(processedChunks);

        // Update chunk count on entity and save
        document.setChunkCount(processedChunks.size());
        Document savedDoc = documentRepository.save(document);

        return mapToResponse(savedDoc);
    }

    @Override
    public List<DocumentResponse> listDocuments(UUID courseId, UserPrincipal currentUser) {
        log.info("Listing documents for course ID: {}", courseId);
        List<Document> documents;
        if (courseId != null) {
            documents = documentRepository.findByCourseId(courseId);
        } else {
            documents = documentRepository.findByUserId(currentUser.getId());
        }
        return documents.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteDocument(UUID documentId, UserPrincipal currentUser) {
        log.info("Deleting document: {}", documentId);
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document", "id", documentId));

        // Authorization check
        if (document.getCourse() != null) {
            checkCoursePermission(document.getCourse(), currentUser);
        } else if (!document.getUser().getId().equals(currentUser.getId())) {
            throw new BusinessException("You do not have permission to delete this document.");
        }

        // 1. Delete vector store chunks
        List<String> idsToDelete = new ArrayList<>();
        for (int i = 0; i < document.getChunkCount(); i++) {
            idsToDelete.add(documentId + "_" + i);
        }
        try {
            log.info("Deleting {} chunks from vector store...", idsToDelete.size());
            vectorStore.delete(idsToDelete);
        } catch (Exception e) {
            log.error("Failed to delete vector embeddings for document {}", documentId, e);
        }

        // 2. Delete document entity from DB
        documentRepository.delete(document);
    }

    private String extractTextFromPdf(byte[] fileBytes) throws IOException {
        try (org.apache.pdfbox.pdmodel.PDDocument pdfDocument = org.apache.pdfbox.Loader.loadPDF(fileBytes)) {
            org.apache.pdfbox.text.PDFTextStripper stripper = new org.apache.pdfbox.text.PDFTextStripper();
            return stripper.getText(pdfDocument);
        }
    }

    private String extractTextFromDocx(java.io.InputStream inputStream) throws IOException {
        try (org.apache.poi.xwpf.usermodel.XWPFDocument docxDocument = new org.apache.poi.xwpf.usermodel.XWPFDocument(inputStream);
             org.apache.poi.xwpf.extractor.XWPFWordExtractor extractor = new org.apache.poi.xwpf.extractor.XWPFWordExtractor(docxDocument)) {
            return extractor.getText();
        }
    }

    private void checkCoursePermission(Course course, UserPrincipal currentUser) {
        if (!currentUser.getRole().name().equals("ADMIN") &&
                !course.getInstructor().getId().equals(currentUser.getId())) {
            throw new BusinessException("You do not have permission to manage documents for this course.");
        }
    }

    private DocumentResponse mapToResponse(Document document) {
        return DocumentResponse.builder()
                .id(document.getId())
                .filename(document.getFilename())
                .contentType(document.getContentType())
                .size(document.getSize())
                .chunkCount(document.getChunkCount())
                .processedAt(document.getProcessedAt())
                .build();
    }
}
