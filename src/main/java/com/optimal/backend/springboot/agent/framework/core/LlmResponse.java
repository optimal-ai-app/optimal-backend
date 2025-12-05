package com.optimal.backend.springboot.agent.framework.core;

import java.util.ArrayList;
import java.util.List;

import dev.langchain4j.data.message.AiMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a response from the LLM which may contain tool calls
 * Compatible with LangChain4j's Response format
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LlmResponse {
    /**
     * The text content of the response
     */
    private String content;

    /**
     * List of tool calls requested by the LLM
     */
    private List<ToolCall> toolCalls;

    /**
     * The original LangChain4j AiMessage for advanced usage
     */
    private AiMessage aiMessage;

    private int tokens = 0;

    /**
     * Constructor with just content (no tool calls)
     * Ensures content is never null
     */
    public LlmResponse(String content) {
        this.content = content != null ? content : "";
        this.toolCalls = new ArrayList<>();
    }

    /**
     * Constructor from LangChain4j AiMessage
     * Ensures content is never null
     */
    public LlmResponse(AiMessage aiMessage, int tokens) {
        this.aiMessage = aiMessage;
        this.toolCalls = extractToolCalls(aiMessage);
        this.content = extractContent(aiMessage);
        this.tokens = tokens;
    }

    public LlmResponse(String content, int tokens) {
        this.content = content != null ? content : "";
        this.toolCalls = new ArrayList<>();
        this.tokens = tokens;
    }

    private String extractContent(AiMessage aiMessage) {
        if (aiMessage == null)
            return "Error: Null AI message received";

        // Get the main message text
        String messageText = aiMessage.text();
        
        // If there's text, return it as-is (don't append tool call info to preserve JSON structure)
        if (messageText != null && !messageText.trim().isEmpty()) {
            return messageText;
        }
        
        // Only include tool call info if there's no text (for debugging/logging purposes)
        if (aiMessage.hasToolExecutionRequests()) {
            StringBuilder content = new StringBuilder();
            content.append("Tool Calls:\n");
            aiMessage.toolExecutionRequests().forEach(request -> {
                content.append("- Tool: ").append(request.name())
                        .append("\n  Arguments: ").append(request.arguments())
                        .append("\n");
            });
            return content.toString();
        }

        // If we get here, there's neither text nor tool calls - this is an error condition
        return "Error: Empty response from LLM (no text or tool calls)";
    }

    private List<ToolCall> extractToolCalls(AiMessage aiMessage) {
        List<ToolCall> calls = new ArrayList<>();

        if (aiMessage != null && aiMessage.hasToolExecutionRequests()) {
            aiMessage.toolExecutionRequests().forEach(request -> {
                ToolCall toolCall = new ToolCall();
                toolCall.setId(request.id());
                toolCall.setName(request.name());
                toolCall.setInput(request.arguments());
                calls.add(toolCall);
            });
        }

        return calls;
    }

    @Override
    public String toString() {
        return "LlmResponse{" +
                "content='" + content + '\'' +
                ", toolCalls=" + toolCalls +
                ", aiMessage=" + aiMessage +
                '}';
    }

    /**
     * Check if this response contains any tool calls
     * 
     * @return true if there are tool calls, false otherwise
     */
    public boolean hasToolCalls() {
        return toolCalls != null && !toolCalls.isEmpty();
    }

    /**
     * Add a tool call to this response
     * 
     * @param toolCall The tool call to add
     */
    public void addToolCall(ToolCall toolCall) {
        if (this.toolCalls == null) {
            this.toolCalls = new ArrayList<>();
        }
        this.toolCalls.add(toolCall);
    }
}