package com.optimal.backend.springboot.agent.framework.tools;

import com.optimal.backend.springboot.agent.framework.core.Tool;
import dev.langchain4j.agent.tool.ToolParameters;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Example tool implementation for demonstration purposes
 * Shows how to define parameters and parse JSON input
 */
@Component
public class ExampleTool implements Tool {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String getName() {
        return "exampleTool";
    }

    @Override
    public String execute(String input) {
        try {
            // Parse JSON input to extract parameters
            JsonNode inputNode = objectMapper.readTree(input);

            String message = inputNode.has("message") ? inputNode.get("message").asText() : "No message provided";
            String category = inputNode.has("category") ? inputNode.get("category").asText() : "general";
            int priority = inputNode.has("priority") ? inputNode.get("priority").asInt() : 1;

            // Process the parameters
            String result = String.format(
                    "Tool executed successfully!\nMessage: %s\nCategory: %s\nPriority: %d\nProcessed at: %s",
                    message, category, priority, java.time.LocalDateTime.now());

            return result;

        } catch (Exception e) {
            return "Error processing tool input: " + e.getMessage() + ". Input was: " + input;
        }
    }

    @Override
    public String getDescription() {
        return "An example tool that processes a message with category and priority. Useful for testing the tool execution framework with structured parameters.";
    }

    @Override
    public ToolParameters getParameters() {
        return ToolParameters.builder()
                .type("object")
                .properties(java.util.Map.of(
                        "message", java.util.Map.of("type", "string", "description", "The message to process"),
                        "category",
                        java.util.Map.of("type", "string", "description", "The category of the message", "enum",
                                Arrays.asList("general", "urgent", "info", "warning")),
                        "priority",
                        java.util.Map.of("type", "number", "description", "Priority level from 1 (low) to 5 (high)")))
                .required(Arrays.asList("message"))
                .build();
    }
}