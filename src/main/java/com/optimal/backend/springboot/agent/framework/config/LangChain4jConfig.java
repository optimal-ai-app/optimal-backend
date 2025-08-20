package com.optimal.backend.springboot.agent.framework.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.DisabledChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;

/**
 * Configuration class for LangChain4j integration
 */
@Configuration
public class LangChain4jConfig {

    @Value("${langchain4j.open-ai.chat-model.api-key:}")
    private String apiKey;

    @Value("${langchain4j.open-ai.chat-model.model-name:gpt-5-mini}")
    private String modelName;

    @Value("${langchain4j.open-ai.chat-model.temperature:1}")
    private Double temperature;

    @Value("${langchain4j.open-ai.chat-model.max-tokens:1000}")
    private Integer maxTokens;

    @Bean
    public ChatModel chatLanguageModel() {
        // Check if API key is properly configured
        if (apiKey == null || apiKey.trim().isEmpty() || apiKey.equals("demo")) {
            System.err.println("WARNING: OpenAI API key is not configured. Using disabled chat model.");
            return new DisabledChatModel();
        }
        System.out.println("\n=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=\n\n");
        System.out.println("\n\nCONFIGURING LLM MODEL\n\n");
        System.out.println("apiKey: " + apiKey);
        System.out.println("modelName: " + modelName);
        System.out.println("temperature: " + temperature);
        System.out.println("maxTokens: " + maxTokens);
        System.out.println("\n=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=\n\n");

        try {
            return OpenAiChatModel.builder()
                    .apiKey(apiKey)
                    .modelName(modelName)
                    .temperature(temperature)
                    // .maxCompletionTokens(maxTokens)
                    .build();
        } catch (Exception e) {
            System.err.println("ERROR: Failed to create OpenAI chat model: " + e.getMessage());
            return new DisabledChatModel();
        }
    }
}