// package com.optimal.backend.springboot.agent.framework.config;

// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;

// import dev.langchain4j.model.chat.ChatModel;
// import dev.langchain4j.model.chat.DisabledChatModel;
// import dev.langchain4j.model.openai.OpenAiChatModel;

// /**
//  * Configuration class for LangChain4j integration
//  */
// @Configuration
// public class LangChain4jConfig {

//     @Value("${langchain4j.gemini.chat-model.api-key:}")
//     private String geminiApiKey;

//     @Value("${langchain4j.open-ai.chat-model.api-key:}")
//     private String gptApiKey;

//     @Value("${langchain4j.open-ai.chat-model.temperature:1}")
//     private Double temperature;

//     @Value("${langchain4j.open-ai.chat-model.max-tokens:2048}")
//     private Integer maxTokens;

//     @Bean
//     public ChatModel chatLanguageModel() {
//         // Check if API key is properly configured
//         if (geminiApiKey == null || geminiApiKey.trim().isEmpty() || geminiApiKey.equals("demo")) {
//             System.err.println("WARNING: OpenAI API key is not configured. Using disabled chat model.");
//             return new DisabledChatModel();
//         }
//         try {
//             return OpenAiChatModel.builder()
//                     .apiKey(gptApiKey)
//                     .modelName("gpt-4o-mini")
//                     .temperature(1.0).logResponses(true).returnThinking(true)
//                     // .maxTokens(maxTokens)
//                     .build();
//         } catch (Exception e) {
//             System.err.println("ERROR: Failed to create OpenAI chat model: " + e.getMessage());
//             return new DisabledChatModel();
//         }
//     }

//     public ChatModel lightChatLanguageModel() {
//         try {
//             return OpenAiChatModel.builder()
//                     .apiKey(gptApiKey)
//                     .modelName("gpt-4o-mini")
//                     .temperature(.2)
//                     .build();
//         } catch (Exception e) {
//             System.err.println("ERROR: Failed to create OpenAI chat model: " +
//                     e.getMessage());
//             return new DisabledChatModel();
//         }
//     }
// }

package com.optimal.backend.springboot.agent.framework.config;

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
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.model.chat.request.json.JsonSchema;
import dev.langchain4j.model.chat.request.json.JsonSchemaElement;
import dev.langchain4j.model.chat.request.json.JsonStringSchema;
import dev.langchain4j.model.chat.request.json.JsonStringSchema;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.googleai.GeminiThinkingConfig;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;

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
        // Check if API key is properly configured
        if (geminiApiKey == null || geminiApiKey.trim().isEmpty() ||
                geminiApiKey.equals("demo")) {
            System.err.println("WARNING: OpenAI API key is not configured. Using disabledchat model.");
            return new DisabledChatModel();
        }
        try {
            return GoogleAiGeminiChatModel.builder()
                    .apiKey(geminiApiKey)
                    .modelName("gemini-2.5-flash")
                    .temperature(temperature).logResponses(true)
                    .returnThinking(true)
                    .build();
        } catch (Exception e) {
            System.err.println("ERROR: Failed to create OpenAI chat model: " +
                    e.getMessage());
            return new DisabledChatModel();
        }
    }

    public ChatModel lightChatLanguageModel() {
        try {
            return OpenAiChatModel.builder()
                    .apiKey(gptApiKey)
                    .modelName("gpt-4o-mini")
                    .temperature(.2)
                    .build();
        } catch (Exception e) {
            System.err.println("ERROR: Failed to create OpenAI chat model: " +
                    e.getMessage());
            return new DisabledChatModel();
        }
    }
    /*
     * public ResponseFormat getResponseFormat() {
     * JsonObjectSchema root = JsonObjectSchema.builder()
     * .addProperties(Map.<String, JsonSchemaElement>of(
     * "content", JsonStringSchema.builder().build(),
     * "tags", JsonArraySchema.builder()
     * .items(JsonStringSchema.builder().build())
     * .build(),
     * "readyToHandoff", JsonBooleanSchema.builder().build()))
     * .additionalProperties(true)
     * .required("content", "tags", "readyToHandoff")
     * .build();
     * 
     * ResponseFormat responseFormat = ResponseFormat.builder()
     * .type(ResponseFormatType.JSON)
     * .jsonSchema(JsonSchema.builder()
     * .name("ContentResponse")
     * .rootElement(root)
     * .build())
     * .build();
     * return responseFormat;
     * 
     * }
     */
}