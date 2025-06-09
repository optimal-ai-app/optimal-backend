package com.optimal.backend.springboot.agent.framework.config;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.chat.DisabledChatLanguageModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for LangChain4j integration
 */
@Configuration
public class LangChain4jConfig {

    @Value("${langchain4j.open-ai.chat-model.api-key:}")
    private String apiKey;

    @Value("${langchain4j.open-ai.chat-model.model-name:gpt-4.1-nano}")
    private String modelName;

    @Value("${langchain4j.open-ai.chat-model.temperature:0.7}")
    private Double temperature;

    @Value("${langchain4j.open-ai.chat-model.max-tokens:1000}")
    private Integer maxTokens;

    @Bean
    public ChatLanguageModel chatLanguageModel() {
        // Check if API key is properly configured
        if (apiKey == null || apiKey.trim().isEmpty() || apiKey.equals("demo")) {
            System.err.println("WARNING: OpenAI API key is not configured. Using disabled chat model.");
            return new DisabledChatLanguageModel();
        }

        try {
            return OpenAiChatModel.builder()
                    .apiKey(apiKey)
                    .modelName(modelName)
                    .temperature(temperature)
                    .maxTokens(maxTokens)
                    .build();
        } catch (Exception e) {
            System.err.println("ERROR: Failed to create OpenAI chat model: " + e.getMessage());
            return new DisabledChatLanguageModel();
        }
    }
}