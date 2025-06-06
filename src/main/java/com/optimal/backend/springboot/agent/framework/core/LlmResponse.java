package com.optimal.backend.springboot.agent.framework.core;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;
import java.util.ArrayList;

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
     */
    public LlmResponse(String content) {
        this.content = content;
        this.toolCalls = new ArrayList<>();
    }

    /**
     * Constructor from LangChain4j AiMessage
     */
    public LlmResponse(AiMessage aiMessage) {
        this.aiMessage = aiMessage;
        this.content = aiMessage.text();
        this.toolCalls = new ArrayList<>();

        // Extract tool calls from AiMessage if they exist
        if (aiMessage.hasToolExecutionRequests()) {
            aiMessage.toolExecutionRequests().forEach(request -> {
                ToolCall toolCall = new ToolCall();
                toolCall.setId(request.id());
                toolCall.setName(request.name());
                toolCall.setInput(request.arguments());
                this.toolCalls.add(toolCall);
            });
        }
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