package com.optimal.backend.springboot.agent.framework.config;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for LangChain4j integration
 */
@Configuration
public class LangChain4jConfig {

    @Value("${langchain4j.open-ai.chat-model.api-key}")
    private String apiKey;

    @Value("${langchain4j.open-ai.chat-model.model-name:gpt-4o-mini}")
    private String modelName;

    @Value("${langchain4j.open-ai.chat-model.temperature:0.7}")
    private Double temperature;

    @Value("${langchain4j.open-ai.chat-model.max-tokens:1000}")
    private Integer maxTokens;

    @Bean
    public ChatLanguageModel chatLanguageModel() {
        // Validate API key is provided
        if (apiKey == null || apiKey.trim().isEmpty() || apiKey.equals("demo")) {
            throw new IllegalStateException(
                    "OpenAI API key is required. Please set the OPENAI_API_KEY environment variable.");
        }

        return OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .temperature(temperature)
                .maxTokens(maxTokens)
                .build();
    }
}