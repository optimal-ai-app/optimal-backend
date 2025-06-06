package com.optimal.backend.springboot.agent.framework.tools;

import com.optimal.backend.springboot.agent.framework.core.Tool;
import dev.langchain4j.agent.tool.ToolParameters;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * Tool for adding new goals to the user's goal list
 * Demonstrates proper parameter handling for LangChain4j integration
 */
@Component
public class AddGoalTool implements Tool {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String getName() {
        return "addGoal";
    }

    @Override
    public String execute(String input) {
        try {
            // Parse JSON input to extract parameters
            JsonNode inputNode = objectMapper.readTree(input);

            String title = inputNode.has("title") ? inputNode.get("title").asText() : null;
            String description = inputNode.has("description") ? inputNode.get("description").asText() : "";
            String category = inputNode.has("category") ? inputNode.get("category").asText() : "personal";
            String priority = inputNode.has("priority") ? inputNode.get("priority").asText() : "medium";
            String deadline = inputNode.has("deadline") ? inputNode.get("deadline").asText() : null;

            // Validate required parameters
            if (title == null || title.trim().isEmpty()) {
                return "Error: Goal title is required and cannot be empty.";
            }

            // Simulate adding goal to database
            String goalId = "goal_" + System.currentTimeMillis();
            String createdAt = LocalDateTime.now().toString();

            // Create response
            StringBuilder result = new StringBuilder();
            result.append("✅ Goal added successfully!\n");
            result.append("Goal ID: ").append(goalId).append("\n");
            result.append("Title: ").append(title).append("\n");
            result.append("Description: ").append(description.isEmpty() ? "No description provided" : description)
                    .append("\n");
            result.append("Category: ").append(category).append("\n");
            result.append("Priority: ").append(priority).append("\n");
            if (deadline != null && !deadline.trim().isEmpty()) {
                result.append("Deadline: ").append(deadline).append("\n");
            }
            result.append("Created: ").append(createdAt);

            return result.toString();

        } catch (Exception e) {
            return "Error adding goal: " + e.getMessage() + ". Please check your input format.";
        }
    }

    @Override
    public String getDescription() {
        return "Add a new goal to the user's goal list. Requires a title and optionally accepts description, category, priority, and deadline.";
    }

    @Override
    public ToolParameters getParameters() {
        return ToolParameters.builder()
                .type("object")
                .properties(java.util.Map.of(
                        "title", java.util.Map.of(
                                "type", "string",
                                "description", "The title/name of the goal (required)"),
                        "description", java.util.Map.of(
                                "type", "string",
                                "description", "Detailed description of the goal (optional)"),
                        "category", java.util.Map.of(
                                "type", "string",
                                "description", "Category of the goal",
                                "enum",
                                Arrays.asList("personal", "work", "health", "education", "finance", "relationships")),
                        "priority", java.util.Map.of(
                                "type", "string",
                                "description", "Priority level of the goal",
                                "enum", Arrays.asList("low", "medium", "high", "critical")),
                        "deadline", java.util.Map.of(
                                "type", "string",
                                "description", "Target completion date (YYYY-MM-DD format, optional)")))
                .required(Arrays.asList("title"))
                .build();
    }
}