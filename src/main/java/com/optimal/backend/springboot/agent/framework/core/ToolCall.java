package com.optimal.backend.springboot.agent.framework.core;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a tool call made by the LLM
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ToolCall {
    /**
     * Unique identifier for this tool call
     */
    private String id;

    /**
     * Name of the tool to be called
     */
    private String name;

    /**
     * Input parameters for the tool
     */
    private String input;
}