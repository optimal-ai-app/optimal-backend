package com.optimal.backend.springboot.agent.framework.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.DisabledChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;

@Configuration
public class LangChain4jConfig {

    @Value("${langchain4j.gemini.chat-model.api-key:}")
    private String geminiApiKey;

    @Value("${langchain4j.open-ai.chat-model.api-key:}")
    private String gptApiKey;

    @Bean
    public ChatModel chatLanguageModel() {
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
                    .modelName("gpt-4.1-mini")
                    .temperature(.2)
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
                    .temperature(.2)
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
                    .modelName("gpt-4.1-mini")
                    .temperature(1.0)
                    .build();
        } catch (Exception e) {
            System.err.println("ERROR: Failed to create Creative chat model: " +
                    e.getMessage());
            return new DisabledChatModel();
        }
    }
}