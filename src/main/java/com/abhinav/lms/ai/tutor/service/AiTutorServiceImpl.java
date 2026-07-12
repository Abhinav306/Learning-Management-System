package com.abhinav.lms.ai.tutor.service;

import com.abhinav.lms.ai.tutor.dto.ChatMessageResponse;
import com.abhinav.lms.ai.tutor.dto.ChatSessionResponse;
import com.abhinav.lms.ai.tutor.dto.CreateSessionRequest;
import com.abhinav.lms.ai.tutor.dto.SendMessageRequest;
import com.abhinav.lms.ai.tutor.entity.AiChatMessage;
import com.abhinav.lms.ai.tutor.entity.AiChatSession;
import com.abhinav.lms.ai.tutor.entity.MessageRole;
import com.abhinav.lms.ai.tutor.mapper.AiTutorMapper;
import com.abhinav.lms.ai.tutor.repository.AiChatMessageRepository;
import com.abhinav.lms.ai.tutor.repository.AiChatSessionRepository;
import com.abhinav.lms.course.entity.Course;
import com.abhinav.lms.course.repository.CourseRepository;
import com.abhinav.lms.exception.BadRequestException;
import com.abhinav.lms.exception.ResourceNotFoundException;
import com.abhinav.lms.security.model.UserPrincipal;
import com.abhinav.lms.user.entity.User;
import com.abhinav.lms.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AiTutorServiceImpl implements AiTutorService {

    private final AiChatSessionRepository chatSessionRepository;
    private final AiChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final AiTutorMapper aiTutorMapper;
    private final ChatModel chatModel;

    @Override
    @Transactional
    public ChatSessionResponse createSession(CreateSessionRequest request, UserPrincipal principal) {
        log.info("Creating new AI chat session for user: {}", principal.getUsername());

        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", principal.getId()));

        Course course = null;
        if (request.getCourseId() != null) {
            course = courseRepository.findById(request.getCourseId())
                    .orElseThrow(() -> new ResourceNotFoundException("Course", "id", request.getCourseId()));
        }

        AiChatSession session = AiChatSession.builder()
                .user(user)
                .course(course)
                .title(request.getTitle())
                .active(true)
                .totalTokens(0)
                .build();

        AiChatSession savedSession = chatSessionRepository.save(session);
        return aiTutorMapper.toResponse(savedSession);
    }

    @Override
    public List<ChatSessionResponse> getUserSessions(UserPrincipal principal) {
        List<AiChatSession> sessions = chatSessionRepository.findByUserIdOrderByUpdatedAtDesc(principal.getId());
        return aiTutorMapper.toSessionResponseList(sessions);
    }

    @Override
    public List<ChatSessionResponse> getUserSessionsByCourse(UUID courseId, UserPrincipal principal) {
        List<AiChatSession> sessions = chatSessionRepository.findByUserIdAndCourseIdOrderByUpdatedAtDesc(principal.getId(), courseId);
        return aiTutorMapper.toSessionResponseList(sessions);
    }

    @Override
    public ChatSessionResponse getSession(UUID sessionId, UserPrincipal principal) {
        AiChatSession session = chatSessionRepository.findByIdAndUserId(sessionId, principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("AiChatSession", "id", sessionId));
        return aiTutorMapper.toResponse(session);
    }

    @Override
    public List<ChatMessageResponse> getSessionMessages(UUID sessionId, UserPrincipal principal) {
        // Verify ownership
        if (!chatSessionRepository.findByIdAndUserId(sessionId, principal.getId()).isPresent()) {
            throw new ResourceNotFoundException("AiChatSession", "id", sessionId);
        }
        List<AiChatMessage> messages = chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
        return aiTutorMapper.toMessageResponseList(messages);
    }

    @Override
    @Transactional
    public Flux<String> sendMessage(UUID sessionId, SendMessageRequest request, UserPrincipal principal) {
        log.info("Sending message to AI chat session: {}", sessionId);

        AiChatSession session = chatSessionRepository.findByIdAndUserId(sessionId, principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("AiChatSession", "id", sessionId));

        if (!session.isActive()) {
            throw new BadRequestException("This chat session is no longer active");
        }

        String userContent = request.getContent();
        int estimatedUserTokens = Math.max(1, userContent.length() / 4);

        // 1. Persist the User's message
        AiChatMessage userMessage = AiChatMessage.builder()
                .session(session)
                .role(MessageRole.USER)
                .content(userContent)
                .tokenCount(estimatedUserTokens)
                .build();
        chatMessageRepository.save(userMessage);

        // 2. Build model prompts (System instruction + History context)
        List<Message> modelMessages = new ArrayList<>();

        // Add System prompt
        String systemInstruction;
        if (session.getCourse() != null) {
            Course course = session.getCourse();
            systemInstruction = String.format(
                    "You are an expert AI academic tutor for the course \"%s\".\n" +
                    "Course Description: %s\n" +
                    "Difficulty Level: %s\n\n" +
                    "Your goal is to help students learn by answering their questions, explaining concepts, " +
                    "and guiding them to find solutions themselves. Use clear, concise explanations and Markdown " +
                    "formatting where appropriate.",
                    course.getTitle(),
                    course.getDescription() != null ? course.getDescription() : "No description available",
                    course.getDifficulty().name()
            );
        } else {
            systemInstruction = "You are a helpful and knowledgeable AI academic tutor.\n" +
                    "Your goal is to help students learn by answering their questions, explaining concepts, " +
                    "and guiding them to find solutions. Use clear, concise explanations and Markdown formatting " +
                    "where appropriate.";
        }
        modelMessages.add(new SystemMessage(systemInstruction));

        // Load recent history (cap at last 20 messages to control token usage)
        List<AiChatMessage> history = chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
        // Exclude the message we just saved to handle history replay, then add history
        // Wait, history includes the userMessage we just saved at the end since we queried after save!
        // That's perfect because the last message is the current user prompt.
        int historySize = history.size();
        int startIdx = Math.max(0, historySize - 20);
        List<AiChatMessage> recentHistory = history.subList(startIdx, historySize);

        for (AiChatMessage msg : recentHistory) {
            if (msg.getRole() == MessageRole.USER) {
                modelMessages.add(new UserMessage(msg.getContent()));
            } else if (msg.getRole() == MessageRole.ASSISTANT) {
                modelMessages.add(new AssistantMessage(msg.getContent()));
            }
        }

        // 3. Setup Response Accumulators and AI metadata capture
        StringBuilder assistantContentBuilder = new StringBuilder();
        AtomicReference<Usage> usageRef = new AtomicReference<>(null);

        // 4. Create the reactive stream
        Prompt prompt = new Prompt(modelMessages);
        Flux<ChatResponse> responseFlux = chatModel.stream(prompt);

        return responseFlux
                .map(response -> {
                    if (response.getMetadata() != null && response.getMetadata().getUsage() != null) {
                        usageRef.set(response.getMetadata().getUsage());
                    }
                    if (response.getResult() != null && response.getResult().getOutput() != null) {
                        String delta = response.getResult().getOutput().getText();
                        return delta != null ? delta : "";
                    }
                    return "";
                })
                .doOnNext(assistantContentBuilder::append)
                .doOnComplete(() -> {
                    String fullResponse = assistantContentBuilder.toString();
                    Usage usage = usageRef.get();
                    int promptTokens = estimatedUserTokens;
                    int completionTokens = Math.max(1, fullResponse.length() / 4);

                    if (usage != null) {
                        if (usage.getPromptTokens() != null) {
                            promptTokens = usage.getPromptTokens().intValue();
                        }
                        if (usage.getCompletionTokens() != null) {
                            completionTokens = usage.getCompletionTokens().intValue();
                        }
                    }

                    // Save the Assistant's response to the database
                    saveAssistantMessageAndTokens(sessionId, fullResponse, promptTokens, completionTokens);
                })
                .doOnError(throwable -> log.error("Error during AI Tutor stream generation: ", throwable));
    }

    @Override
    @Transactional
    public void deleteSession(UUID sessionId, UserPrincipal principal) {
        log.info("Deleting AI chat session: {}", sessionId);

        AiChatSession session = chatSessionRepository.findByIdAndUserId(sessionId, principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("AiChatSession", "id", sessionId));

        chatSessionRepository.delete(session);
    }

    // Helper method to write message & update tokens in a self-contained transaction
    private void saveAssistantMessageAndTokens(UUID sessionId, String content, int promptTokens, int completionTokens) {
        try {
            log.info("Saving AI assistant response. Prompt tokens: {}, Completion tokens: {}", promptTokens, completionTokens);

            // Fetch session inside this thread's current transaction context
            AiChatSession session = chatSessionRepository.findById(sessionId).orElse(null);
            if (session == null) {
                log.error("Failed to find session {} when saving assistant response", sessionId);
                return;
            }

            AiChatMessage assistantMessage = AiChatMessage.builder()
                    .session(session)
                    .role(MessageRole.ASSISTANT)
                    .content(content)
                    .tokenCount(completionTokens)
                    .build();
            chatMessageRepository.save(assistantMessage);

            session.setTotalTokens(session.getTotalTokens() + promptTokens + completionTokens);
            chatSessionRepository.save(session);
        } catch (Exception e) {
            log.error("Failed to save assistant message for session: {}", sessionId, e);
        }
    }
}
