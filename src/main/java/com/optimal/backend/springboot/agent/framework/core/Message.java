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
    private int tokens = 0;

    // Store original LangChain4j message to preserve tool calls
    private ChatMessage originalLangChain4jMessage;

    public Message(String role, String message) {
        this.role = role;
        this.message = message;
    }

    public Message(String role, String message, String content, int tokens) {
        this.role = role;
        this.message = message;
        this.content = content;
        this.tokens = tokens;
    }

    /**
     * Constructor to create Message from LangChain4j AiMessage while preserving
     * tool calls
     */
    public Message(AiMessage aiMessage) {
        this.role = "assistant";
        this.message = aiMessage.text();
        this.content = aiMessage.text();
        this.originalLangChain4jMessage = aiMessage; // Preserve original for tool calls
    }

    /**
     * Convert this Message to a LangChain4j ChatMessage
     * Ensures that null values are never passed to LangChain4j
     */
    public ChatMessage toLangChain4jMessage() {
        // If we have an original LangChain4j message, use it to preserve tool calls
        if (originalLangChain4jMessage != null) {
            return originalLangChain4jMessage;
        }

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
                // Tool execution results need to be properly formatted for LangChain4j
                if (toolExecutionId != null && !toolExecutionId.trim().isEmpty()) {
                    return new ToolExecutionResultMessage(toolExecutionId, toolCallId, text);
                } else {
                    // Fallback to UserMessage if no toolExecutionId
                    return new UserMessage("Tool result: " + text);
                }
            default:
                return new UserMessage(text);
        }
    }

    /**
     * Get the text content of this message, never returning null
     */
    public String getTextContent() {
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
        if (chatMessage instanceof SystemMessage) {
            SystemMessage systemMsg = (SystemMessage) chatMessage;
            Message message = new Message("system", systemMsg.text(), systemMsg.text(), 0);
            return message;
        } else if (chatMessage instanceof UserMessage) {
            UserMessage userMsg = (UserMessage) chatMessage;
            Message message = new Message("user", userMsg.singleText(), userMsg.singleText(), 0);
            return message;
        } else if (chatMessage instanceof AiMessage) {
            AiMessage aiMsg = (AiMessage) chatMessage;
            // Use special constructor to preserve tool calls
            return new Message(aiMsg);
        } else if (chatMessage instanceof ToolExecutionResultMessage) {
            ToolExecutionResultMessage toolMsg = (ToolExecutionResultMessage) chatMessage;
            Message message = new Message("tool", toolMsg.text(), toolMsg.text(), 0);
            message.setToolExecutionId(toolMsg.id());
            return message;
        } else {
            return new Message("user", "Unknown message type", "Unknown message type", 0);
        }
    }

    @Override
    public String toString() {
        return "Message{" +
                "content='" + content + '\'' +
                ", toolCallId='" + toolCallId + '\'' +
                ", toolExecutionId='" + toolExecutionId + '\'' +
                ", tokens='" + tokens + '\'' +
                '}';
    }
}
