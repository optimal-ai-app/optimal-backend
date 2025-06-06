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

    @Value("${langchain4j.open-ai.chat-model.api-key:demo}")
    private String apiKey;

    @Value("${langchain4j.open-ai.chat-model.model-name:gpt-3.5-turbo}")
    private String modelName;

    @Value("${langchain4j.open-ai.chat-model.temperature:0.7}")
    private Double temperature;

    @Value("${langchain4j.open-ai.chat-model.max-tokens:1000}")
    private Integer maxTokens;

    @Bean
    public ChatLanguageModel chatLanguageModel() {
        // Only create a real model if API key is provided and not "demo"
        if (apiKey != null && !apiKey.equals("demo") && !apiKey.trim().isEmpty()) {
            return OpenAiChatModel.builder()
                    .apiKey(apiKey)
                    .modelName(modelName)
                    .temperature(temperature)
                    .maxTokens(maxTokens)
                    .build();
            /**
             * // For Azure OpenAI
             * return AzureOpenAiChatModel.builder()
             * .endpoint("your-azure-endpoint")
             * .apiKey(apiKey)
             * .deploymentName("your-deployment")
             * .build();
             * 
             * // For Anthropic Claude
             * return AnthropicChatModel.builder()
             * .apiKey(apiKey)
             * .modelName("claude-3-sonnet")
             * .build();
             */
        } else {
            // Return a mock implementation for demo purposes
            return new MockChatLanguageModel();
        }
    }

    /**
     * Mock implementation for when no real API key is provided
     */
    private static class MockChatLanguageModel implements ChatLanguageModel {
        @Override
        public dev.langchain4j.model.output.Response<dev.langchain4j.data.message.AiMessage> generate(
                java.util.List<dev.langchain4j.data.message.ChatMessage> messages) {
            dev.langchain4j.data.message.AiMessage mockResponse = new dev.langchain4j.data.message.AiMessage(
                    "Mock response from LangChain4j. Configure a real API key to use actual LLM.");
            return new dev.langchain4j.model.output.Response<>(mockResponse);
        }

        @Override
        public dev.langchain4j.model.output.Response<dev.langchain4j.data.message.AiMessage> generate(
                java.util.List<dev.langchain4j.data.message.ChatMessage> messages,
                java.util.List<dev.langchain4j.agent.tool.ToolSpecification> toolSpecifications) {
            dev.langchain4j.data.message.AiMessage mockResponse = new dev.langchain4j.data.message.AiMessage(
                    "Mock response with tools from LangChain4j. Configure a real API key to use actual LLM.");
            return new dev.langchain4j.model.output.Response<>(mockResponse);
        }
    }
}