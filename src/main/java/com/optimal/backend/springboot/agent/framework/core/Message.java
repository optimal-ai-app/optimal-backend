package com.optimal.backend.springboot.agent.framework.core;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.data.message.UserMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    private String role;
    private String message;
    private String content;
    private String toolCallId;
    private String toolExecutionId;

    public Message(String role, String message) {
        this.role = role;
        this.message = message;
    }

    public Message(String role, String message, String content) {
        this.role = role;
        this.message = message;
        this.content = content;
    }

    /**
     * Convert this Message to a LangChain4j ChatMessage
     * Ensures that null values are never passed to LangChain4j
     */
    public ChatMessage toLangChain4jMessage() {
        String text = getTextContent();

        switch (role.toLowerCase()) {
            case "system":
                return new SystemMessage(text);
            case "user":
                return new UserMessage(text);
            case "assistant":
            case "ai":
                return new AiMessage(text);
            case "tool":
                return new UserMessage(text);
            default:
                return new UserMessage(text);
        }
    }

    /**
     * Get the text content of this message, never returning null
     */
    private String getTextContent() {
        if (content != null && !content.trim().isEmpty()) {
            return content;
        }
        if (message != null && !message.trim().isEmpty()) {
            return message;
        }
        // Fallback for null/empty content and message
        return "No content provided";
    }

    /**
     * Create Message from LangChain4j ChatMessage
     */
    public static Message fromLangChain4jMessage(ChatMessage chatMessage) {
        Message message = new Message();

        switch (chatMessage) {
            case SystemMessage systemMsg -> {
                message.setRole("system");
                message.setContent(systemMsg.text());
                message.setMessage(systemMsg.text());
            }
            case UserMessage userMsg -> {
                message.setRole("user");
                message.setContent(userMsg.text());
                message.setMessage(userMsg.text());
            }
            case AiMessage aiMsg -> {
                message.setRole("assistant");
                message.setContent(aiMsg.text());
                message.setMessage(aiMsg.text());
            }
            case ToolExecutionResultMessage toolMsg -> {
                message.setRole("tool");
                message.setContent(toolMsg.text());
                message.setMessage(toolMsg.text());
                message.setToolExecutionId(toolMsg.id());
            }
            default -> {
                message.setRole("user");
                message.setContent("Unknown message type");
                message.setMessage("Unknown message type");
            }
        }

        return message;
    }
}
