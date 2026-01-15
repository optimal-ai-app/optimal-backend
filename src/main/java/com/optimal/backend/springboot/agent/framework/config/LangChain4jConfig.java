package com.optimal.backend.springboot.agent.framework.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.DisabledChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
// import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;

/**
 * Configuration class for LangChain4j integration
 */
@Configuration
public class LangChain4jConfig {

    @Value("${langchain4j.gemini.chat-model.api-key:}")
    private String geminiApiKey;

    @Value("${langchain4j.open-ai.chat-model.api-key:}")
    private String gptApiKey;

    @Value("${langchain4j.open-ai.chat-model.temperature:0.85}")
    private Double temperature;

    @Value("${langchain4j.open-ai.chat-model.max-tokens:2048}")
    private Integer maxTokens;

    @Bean
    public ChatModel chatLanguageModel() {
        if (gptApiKey == null || gptApiKey.trim().isEmpty()) {
            System.err.println("WARNING: OpenAI API key is not configured. Using disabled chat model.");
            return new DisabledChatModel();
        }
        // try {
        // return GoogleAiGeminiChatModel.builder()
        // .apiKey(geminiApiKey)
        // .modelName("gemini-2.5-flash")
        // .temperature(.2)
        // .logResponses(true)
        // .returnThinking(true)
        // .build();
        // } catch (Exception e) {
        // System.err.println("ERROR: Failed to create Main chat model: " +
        // e.getMessage());
        // return new DisabledChatModel();
        // }
        try {
            return OpenAiChatModel.builder()
                    .apiKey(gptApiKey)
                    .modelName("gpt-4.1")
                    .temperature(0.1)
                    .logResponses(true)
                    .strictTools(true)
                    .build();
        } catch (Exception e) {
            System.err.println("ERROR: Failed to create Light chat model: " +
                    e.getMessage());
            return new DisabledChatModel();
        }
    }

    public ChatModel lightChatLanguageModel() {
        try {
            return OpenAiChatModel.builder()
                    .apiKey(gptApiKey)
                    .modelName("gpt-4.1-mini")
                    .temperature(0.1)
                    .build();
        } catch (Exception e) {
            System.err.println("ERROR: Failed to create Light chat model: " +
                    e.getMessage());
            return new DisabledChatModel();
        }
    }

    public ChatModel creativeChatModel() {
        try {
            return OpenAiChatModel.builder()
                    .apiKey(gptApiKey)
                    .modelName("gpt-4.1")
                    .temperature(0.7)
                    .build();
        } catch (Exception e) {
            System.err.println("ERROR: Failed to create Creative chat model: " +
                    e.getMessage());
            return new DisabledChatModel();
        }
    }
}
