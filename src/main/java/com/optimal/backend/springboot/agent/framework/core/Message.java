package com.optimal.backend.springboot.agent.framework.core;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
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
     */
    public ChatMessage toLangChain4jMessage() {
        switch (role.toLowerCase()) {
            case "system":
                return new SystemMessage(content != null ? content : message);
            case "user":
                return new UserMessage(content != null ? content : message);
            case "assistant":
            case "ai":
                return new AiMessage(content != null ? content : message);
            case "tool":
                // For tool messages, we'll just convert to UserMessage for now
                // The proper ToolExecutionResultMessage requires a ToolExecutionRequest
                return new UserMessage(content != null ? content : message);
            default:
                return new UserMessage(content != null ? content : message);
        }
    }

    /**
     * Create Message from LangChain4j ChatMessage
     */
    public static Message fromLangChain4jMessage(ChatMessage chatMessage) {
        Message message = new Message();

        if (chatMessage instanceof SystemMessage) {
            message.setRole("system");
            message.setContent(((SystemMessage) chatMessage).text());
            message.setMessage(((SystemMessage) chatMessage).text());
        } else if (chatMessage instanceof UserMessage) {
            message.setRole("user");
            message.setContent(((UserMessage) chatMessage).text());
            message.setMessage(((UserMessage) chatMessage).text());
        } else if (chatMessage instanceof AiMessage) {
            message.setRole("assistant");
            message.setContent(((AiMessage) chatMessage).text());
            message.setMessage(((AiMessage) chatMessage).text());
        } else if (chatMessage instanceof ToolExecutionResultMessage) {
            ToolExecutionResultMessage toolMsg = (ToolExecutionResultMessage) chatMessage;
            message.setRole("tool");
            message.setContent(toolMsg.text());
            message.setMessage(toolMsg.text());
            message.setToolExecutionId(toolMsg.id());
        }

        return message;
    }
}
