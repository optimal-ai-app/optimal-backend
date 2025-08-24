package com.optimal.backend.springboot.agent.framework.core;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.service.AiServices;

@Component
public class LlmClient {

    private ChatModel chatModel;

    @Autowired
    public LlmClient(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    public void overrideChatModel(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    /**
     * Generate response without tools using direct ChatModel
     */
    public LlmResponse generate(String systemPrompt, List<Message> messages) {
        try {
            List<ChatMessage> allMessages = new ArrayList<>();
            System.out.println("=== Generating Standard Response ===");

            if (systemPrompt != null && !systemPrompt.isEmpty()) {
                allMessages.add(SystemMessage.from(systemPrompt));
            }
            allMessages.addAll(messages.stream().map(Message::toLangChain4jMessage).collect(Collectors.toList()));
            ChatResponse response = chatModel.chat(allMessages);
            AiMessage aiMessage = response.aiMessage();
            return new LlmResponse(aiMessage);
        } catch (Exception e) {
            System.err.println("LangChain4j error: " + e.getMessage());
            return new LlmResponse("LLM ERROR");
        }
    }

    /**
     * Generate response with tools using AiService
     */
    public LlmResponse generate(String systemPrompt, List<Message> messages, List<Object> tools) {
        try {
            System.out.println("=== Generating Standard Response with Tools ===");
            // Create chat memory and add existing messages
            ChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(100);

            // Add system message if provided
            if (systemPrompt != null && !systemPrompt.isEmpty()) {
                chatMemory.add(SystemMessage.from(systemPrompt));
            }

            // Add all messages to memory
            messages.forEach(message -> chatMemory.add(message.toLangChain4jMessage()));

            // Create AI Service with tools
            ToolCapableAssistant assistant = AiServices.builder(ToolCapableAssistant.class)
                    .chatModel(chatModel)
                    .chatMemory(chatMemory)
                    .tools(tools)
                    .build();

            // Get the latest user message to send to assistant
            String userInput = getLatestUserMessage(messages);

            // Generate response using AI Service
            String response = assistant.chat(userInput);

            // Convert response to LlmResponse
            // Note: AiServices handles tool execution internally, so we get the final
            // response
            return new LlmResponse(response);

        } catch (Exception e) {
            System.err.println("Error generating response with tools: " + e.getMessage());
            e.printStackTrace();
            return new LlmResponse("Error generating response with tools: " + e.getMessage());
        }
    }

    /**
     * Extract the latest user message from the message list
     */
    private String getLatestUserMessage(List<Message> messages) {
        if (messages == null || messages.isEmpty()) {
            return "Hello";
        }

        // Find the last user message
        for (int i = messages.size() - 1; i >= 0; i--) {
            Message message = messages.get(i);
            if ("user".equalsIgnoreCase(message.getRole())) {
                return message.getContent() != null ? message.getContent()
                        : message.getMessage() != null ? message.getMessage() : "Hello";
            }
        }

        // Fallback: return the last message content
        Message lastMessage = messages.get(messages.size() - 1);
        return lastMessage.getContent() != null ? lastMessage.getContent()
                : lastMessage.getMessage() != null ? lastMessage.getMessage() : "Hello";
    }

    /**
     * Assistant interface for tool-capable AI services.
     * This interface is used by AiServices to handle chat with tool support.
     */
    public interface ToolCapableAssistant {
        String chat(String userMessage);
    }

}
