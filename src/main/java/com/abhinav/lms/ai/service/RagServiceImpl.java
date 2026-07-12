package com.abhinav.lms.ai.service;

import com.abhinav.lms.ai.dto.RagQueryRequest;
import com.abhinav.lms.ai.dto.RagQueryResponse;
import com.abhinav.lms.exception.BusinessException;
import com.abhinav.lms.security.model.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RagServiceImpl implements RagService {

    private final ChatModel chatModel;
    private final VectorStore vectorStore;

    @Override
    public RagQueryResponse queryRag(RagQueryRequest request, UserPrincipal currentUser) {
        log.info("Processing RAG query: {}, courseId: {}", request.getQuery(), request.getCourseId());

        // 1. Perform similarity search in Vector Store (Retrieve)
        // Fetch slightly more candidates to handle client-side course filtering robustly across all stores
        List<org.springframework.ai.document.Document> similarDocs;
        try {
            similarDocs = vectorStore.similaritySearch(
                    SearchRequest.builder()
                            .query(request.getQuery())
                            .topK(10)
                            .build()
            );
        } catch (Exception e) {
            log.error("Failed to perform similarity search in vector store", e);
            throw new BusinessException("Vector store search failed: " + e.getMessage());
        }

        // Apply metadata filters dynamically for safety (especially on in-memory stores)
        if (request.getCourseId() != null) {
            similarDocs = similarDocs.stream()
                    .filter(d -> d.getMetadata() != null &&
                            request.getCourseId().toString().equals(d.getMetadata().get("courseId")))
                    .collect(Collectors.toList());
        }

        // Limit candidates back to top 4 after filtering
        similarDocs = similarDocs.stream().limit(4).collect(Collectors.toList());

        // 2. Build local document context (Augment)
        String context = similarDocs.stream()
                .map(org.springframework.ai.document.Document::getText)
                .filter(Objects::nonNull)
                .collect(Collectors.joining("\n\n"));

        // If no relevant documents were found, return fallback directly
        if (context.trim().isEmpty()) {
            return RagQueryResponse.builder()
                    .answer("No relevant information found in the uploaded documents. Please ensure the documents are processed or contain details related to your question.")
                    .sources(List.of())
                    .build();
        }

        // 3. Orchestrate LLM request (Generate)
        String systemInstruction =
                "You are a helpful learning assistant for the Learning Management System (LMS).\n" +
                "Answer the student's question using ONLY the provided document contexts.\n" +
                "If the answer cannot be found in the contexts, politely state that the information is not available in the uploaded documents.\n" +
                "Do not make up information or use external knowledge beyond the provided contexts.";

        String userContent = String.format(
                "CONTEXTS:\n%s\n\nQUESTION:\n%s",
                context,
                request.getQuery()
        );

        Prompt prompt = new Prompt(List.of(
                new SystemMessage(systemInstruction),
                new UserMessage(userContent)
        ));

        log.info("Calling ChatModel for RAG answer generation...");
        ChatResponse response = chatModel.call(prompt);

        if (response.getResult() == null || response.getResult().getOutput() == null) {
            throw new BusinessException("Failed to generate answer from the AI model.");
        }

        String answer = response.getResult().getOutput().getText();

        // 4. Extract source files for attribution
        List<String> sources = similarDocs.stream()
                .map(d -> d.getMetadata() != null ? (String) d.getMetadata().get("filename") : null)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        return RagQueryResponse.builder()
                .answer(answer)
                .sources(sources)
                .build();
    }
}
