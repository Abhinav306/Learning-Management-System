package com.abhinav.lms.ai.recommendation.service;

import com.abhinav.lms.ai.recommendation.dto.RecommendationResponse;
import com.abhinav.lms.course.entity.Course;
import com.abhinav.lms.course.entity.CourseStatus;
import com.abhinav.lms.course.repository.CourseRepository;
import com.abhinav.lms.enrollment.entity.Enrollment;
import com.abhinav.lms.enrollment.repository.EnrollmentRepository;
import com.abhinav.lms.security.model.UserPrincipal;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class RecommendationServiceImpl implements RecommendationService {

    private final ChatModel chatModel;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ObjectMapper objectMapper;

    @Override
    public List<RecommendationResponse> getPersonalizedRecommendations(UserPrincipal currentUser, int limit) {
        log.info("Fetching personalized course recommendations for student: {}", currentUser.getEmail());

        // 1. Get student enrollment history
        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(currentUser.getId(), Pageable.unpaged()).getContent();

        // 2. Cold Start check: If user has no enrollments, immediately return popular courses
        if (enrollments.isEmpty()) {
            log.info("Student has no enrollment history. Falling back to popular courses.");
            return getPopularCourses(limit);
        }

        // 3. Gather candidate courses (published and not already enrolled)
        List<Course> allPublished = courseRepository.findByStatus(CourseStatus.PUBLISHED, Pageable.unpaged()).getContent();
        Set<UUID> enrolledCourseIds = enrollments.stream()
                .map(e -> e.getCourse().getId())
                .collect(Collectors.toSet());

        List<Course> candidates = allPublished.stream()
                .filter(c -> !enrolledCourseIds.contains(c.getId()))
                .collect(Collectors.toList());

        // If there are no candidate courses left to recommend, return empty list
        if (candidates.isEmpty()) {
            log.info("No candidates available for recommendations.");
            return new ArrayList<>();
        }

        // 4. Orchestrate LLM Prompt
        try {
            String enrolledHistoryText = enrollments.stream()
                    .map(e -> String.format("- Title: %s, Category: %s, Description: %s",
                            e.getCourse().getTitle(),
                            e.getCourse().getCategory() != null ? e.getCourse().getCategory().getName() : "General",
                            e.getCourse().getShortDescription() != null ? e.getCourse().getShortDescription() : ""))
                    .collect(Collectors.joining("\n"));

            String candidatesText = candidates.stream()
                    .map(c -> String.format("- CourseID: %s, Title: %s, Category: %s, Difficulty: %s, Description: %s",
                            c.getId().toString(),
                            c.getTitle(),
                            c.getCategory() != null ? c.getCategory().getName() : "General",
                            c.getDifficulty().name(),
                            c.getShortDescription() != null ? c.getShortDescription() : ""))
                    .collect(Collectors.joining("\n"));

            String systemInstruction = String.format(
                    "You are a helpful course recommendations engine for a Learning Management System.\n" +
                    "Your task is to recommend up to %d courses to a student based on their historical course enrollments.\n" +
                    "Analyze the student's preferences (e.g., categories they like, difficulty levels) and select the most relevant matching candidate courses.\n" +
                    "Follow these strict rules:\n" +
                    "1. Recommend at most %d courses from the candidates list.\n" +
                    "2. Format: You MUST respond ONLY with a raw JSON array matching this format (no markdown, no backticks, no comments, just raw JSON):\n" +
                    "[\n" +
                    "  {\n" +
                    "    \"courseId\": \"string-uuid\",\n" +
                    "    \"reason\": \"A brief 1-sentence personalized explanation showing how it aligns with their interest in category X or topic Y.\"\n" +
                    "  }\n" +
                    "]",
                    limit,
                    limit
            );

            String userPrompt = String.format(
                    "STUDENT ENROLLMENT HISTORY:\n%s\n\nCANDIDATE COURSES AVAILABLE:\n%s",
                    enrolledHistoryText,
                    candidatesText
            );

            Prompt prompt = new Prompt(List.of(
                    new SystemMessage(systemInstruction),
                    new UserMessage(userPrompt)
            ));

            log.info("Calling ChatModel for personalized recommendations...");
            ChatResponse response = chatModel.call(prompt);

            if (response.getResult() != null && response.getResult().getOutput() != null) {
                String rawJson = response.getResult().getOutput().getText();
                String cleanedJson = cleanJsonString(rawJson);

                List<LlmRecommendation> llmRecommendations = objectMapper.readValue(
                        cleanedJson,
                        new TypeReference<List<LlmRecommendation>>() {}
                );

                if (llmRecommendations != null && !llmRecommendations.isEmpty()) {
                    Map<UUID, String> reasonMap = llmRecommendations.stream()
                            .filter(r -> r.getCourseId() != null)
                            .collect(Collectors.toMap(
                                    r -> UUID.fromString(r.getCourseId()),
                                    LlmRecommendation::getReason,
                                    (r1, r2) -> r1
                            ));

                    return candidates.stream()
                            .filter(c -> reasonMap.containsKey(c.getId()))
                            .map(c -> mapToResponse(c, reasonMap.get(c.getId())))
                            .limit(limit)
                            .collect(Collectors.toList());
                }
            }
        } catch (Exception e) {
            log.error("Failed to generate personalized recommendations. Falling back to popular courses.", e);
        }

        // Fallback to popular courses in case of failure
        return getPopularCourses(limit);
    }

    @Override
    public List<RecommendationResponse> getPopularCourses(int limit) {
        log.info("Fetching popular courses with limit: {}", limit);
        List<Course> popularCourses = courseRepository.findPopularCourses(PageRequest.of(0, limit));

        return popularCourses.stream()
                .map(c -> mapToResponse(c, "Popular choice among students on our platform."))
                .collect(Collectors.toList());
    }

    private RecommendationResponse mapToResponse(Course course, String reason) {
        return RecommendationResponse.builder()
                .id(course.getId())
                .title(course.getTitle())
                .shortDescription(course.getShortDescription())
                .thumbnailUrl(course.getThumbnailUrl())
                .price(course.getPrice())
                .difficulty(course.getDifficulty())
                .categoryName(course.getCategory() != null ? course.getCategory().getName() : "General")
                .reason(reason)
                .build();
    }

    private String cleanJsonString(String rawJson) {
        if (rawJson == null) {
            return "";
        }
        String cleaned = rawJson.trim();
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7);
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3);
        }
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3);
        }
        return cleaned.trim();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LlmRecommendation {
        private String courseId;
        private String reason;
    }
}
