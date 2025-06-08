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
    public LlmResponse(AiMessage aiMessage) {
        this.aiMessage = aiMessage;
        this.content = extractContent(aiMessage);
        this.toolCalls = extractToolCalls(aiMessage);
    }

    private String extractContent(AiMessage aiMessage) {
        if (aiMessage == null) return "";
        
        String messageText = aiMessage.text();
        return (messageText != null && !messageText.trim().isEmpty()) ? messageText : "";
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