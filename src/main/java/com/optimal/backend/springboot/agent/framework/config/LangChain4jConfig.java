package com.optimal.backend.springboot.agent.framework.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.DisabledChatModel;
import dev.langchain4j.model.chat.request.ResponseFormat;
import dev.langchain4j.model.chat.request.ResponseFormatType;
import dev.langchain4j.model.chat.request.json.JsonArraySchema;
import dev.langchain4j.model.chat.request.json.JsonBooleanSchema;
import dev.langchain4j.model.chat.request.json.JsonNumberSchema;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.model.chat.request.json.JsonSchema;
import dev.langchain4j.model.chat.request.json.JsonSchemaElement;
import dev.langchain4j.model.chat.request.json.JsonStringSchema;
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

        try {
            return OpenAiChatModel.builder()
                    .apiKey(gptApiKey)
                    .modelName("gpt-4.1")
                    .temperature(0.2)
                    .logResponses(true)
                    .strictTools(true)
                    .strictJsonSchema(true)
                    .responseFormat(getResponseFormat())
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

    private ResponseFormat getResponseFormat() {
        JsonArraySchema tagsSchema = JsonArraySchema.builder()
                .items(JsonStringSchema.builder()
                        .description(
                                "A raw JSON string containing the arbitrary data object. Do not escape quotes unnecessarily.")
                        .build())
                .build();

        JsonSchemaElement dataSchema = JsonStringSchema.builder()
                .description("A raw JSON string containing dynamic key-value pairs (e.g.,{\"goalId\": 123}).")
                .build();

        Map<String, JsonSchemaElement> jsonProperties = new HashMap<>();

        jsonProperties.put("content", JsonStringSchema.builder().build());
        jsonProperties.put("readyToHandoff", JsonBooleanSchema.builder().build());
        jsonProperties.put("reInterpret", JsonBooleanSchema.builder().build());
        jsonProperties.put("currentStep", JsonNumberSchema.builder().build());
        jsonProperties.put("tags", tagsSchema);
        jsonProperties.put("data", dataSchema);

        return ResponseFormat.builder()
                .type(ResponseFormatType.JSON)
                .jsonSchema(JsonSchema.builder()
                        .name("ChatResponse")
                        .rootElement(JsonObjectSchema.builder()
                                .addProperties(jsonProperties)
                                .required("content", "readyToHandoff", "reInterpret")
                                .build())
                        .build())
                .build();
    }
}
