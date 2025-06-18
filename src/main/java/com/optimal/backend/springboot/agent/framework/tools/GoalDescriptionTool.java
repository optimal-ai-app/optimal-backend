package com.optimal.backend.springboot.agent.framework.tools;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.optimal.backend.springboot.agent.framework.core.Tool;
import com.optimal.backend.springboot.domain.entity.Goal;
import com.optimal.backend.springboot.service.GoalService;

import dev.langchain4j.agent.tool.ToolParameters;

@Component
public class GoalDescriptionTool implements Tool {
    @Override
    public String getName() {
        return "goalDescriptionTool";
    }

    @Autowired
    private GoalService goalService;

    @Override
    public String execute(String input) {
        try {
            JsonNode inputNode = new ObjectMapper().readTree(input);
            UUID userId = UUID.fromString(inputNode.get("userId").asText());

            List<Goal> goals = goalService.getGoalsByUser(userId);

            if (goals.isEmpty()) {
                return "No goals found for this user.";
            }

            StringBuilder response = new StringBuilder();
            response.append("Here are your goals:\n\n");

            for (Goal goal : goals) {
                response.append("Title: ").append(goal.getTitle()).append("\n");
                response.append("Description: ").append(goal.getDescription()).append("\n\n");
                response.append("Due Date: ").append(goal.getDueDate()).append("\n\n");
                response.append("ID: ").append(goal.getId()).append("\n\n");
            }

            return response.toString();
        } catch (Exception e) {
            return "Error retrieving goals: " + e.getMessage();
        }
    }

    @Override
    public String getDescription() {
        return "This tool queries the user's goals and returns a list of the goals names and descriptions";
    }

    @Override
    public ToolParameters getParameters() {
        return ToolParameters.builder()
                .type("object")
                .properties(Map.of(
                        "userId", Map.of("type", "string", "description", "The user's UUID")))
                .required(Arrays.asList("userId"))
                .build();
    }
}
