package com.abhinav.lms.config;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@ConditionalOnProperty(name = "spring.ai.openai.api-key", havingValue = "dev-key-not-set", matchIfMissing = true)
public class MockAiConfig {

    @Bean
    @Primary
    public EmbeddingModel mockEmbeddingModel() {
        return new EmbeddingModel() {
            @Override
            public float[] embed(String text) {
                // Return a dummy 1536-dimension float vector (all 0.01)
                float[] vector = new float[1536];
                for (int i = 0; i < 1536; i++) {
                    vector[i] = 0.01f;
                }
                return vector;
            }

            @Override
            public float[] embed(org.springframework.ai.document.Document document) {
                return embed(document.getText());
            }

            @Override
            public EmbeddingResponse call(EmbeddingRequest request) {
                List<Embedding> embeddings = request.getInstructions().stream()
                        .map(text -> new Embedding(embed(text), 0))
                        .collect(Collectors.toList());
                return new EmbeddingResponse(embeddings);
            }
        };
    }

    @Bean
    @Primary
    public ChatModel mockChatModel() {
        return new ChatModel() {
            @Override
            public ChatResponse call(Prompt prompt) {
                String userQuery = prompt.getInstructions().get(prompt.getInstructions().size() - 1).getText();
                String mockResponse = "🤖 **[AuraLMS AI Offline Mode]**\n\nI received your query: \"" + userQuery + 
                        "\".\n\nTo enable actual GPT-4 answers, please configure your OpenAI API Key by setting the `SPRING_AI_OPENAI_API_KEY` environment variable in your system, or update it in the backend `application-dev.yml` file.";
                
                Generation generation = new Generation(new AssistantMessage(mockResponse));
                return new ChatResponse(List.of(generation));
            }

            @Override
            public Flux<ChatResponse> stream(Prompt prompt) {
                String userQuery = prompt.getInstructions().get(prompt.getInstructions().size() - 1).getText();
                String mockResponse = "🤖 **[AuraLMS AI Offline Mode]**\n\nI received your query: \"" + userQuery + 
                        "\".\n\nTo enable actual GPT-4 answers, please configure your OpenAI API Key by setting the `SPRING_AI_OPENAI_API_KEY` environment variable in your system.";
                
                // Stream the response word by word
                String[] words = mockResponse.split(" ");
                List<ChatResponse> responses = new ArrayList<>();
                for (String word : words) {
                    Generation gen = new Generation(new AssistantMessage(word + " "));
                    responses.add(new ChatResponse(List.of(gen)));
                }
                
                return Flux.fromIterable(responses)
                        .delayElements(java.time.Duration.ofMillis(60));
            }
        };
    }
}
