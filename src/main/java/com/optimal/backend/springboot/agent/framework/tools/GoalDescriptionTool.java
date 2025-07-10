package com.optimal.backend.springboot.agent.framework.tools;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.optimal.backend.springboot.agent.framework.core.Tool;
import com.optimal.backend.springboot.agent.framework.core.UserContext;
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
            UUID userId = UserContext.requireUserId();
            System.out.println("=== GoalDescriptionTool: Using userId from context: " + userId);

            List<Goal> goals = goalService.getGoalsByUser(userId);

            if (goals.isEmpty()) {
                return "No goals found for this user.";
            }

            StringBuilder response = new StringBuilder();
            response.append("Here are your goals:\n\n");

            for (int i = 0; i < goals.size(); i++) {
                Goal goal = goals.get(i);
                response.append("**Goal ").append(i + 1).append(":**\n");
                response.append("- Title: ").append(goal.getTitle()).append("\n");
                response.append("- Description: ").append(goal.getDescription()).append("\n");
                response.append("- End Date: ").append(goal.getDueDate()).append("\n");
                response.append("- Status: ").append(goal.getStatus()).append("\n\n");
                response.append("- Goal UUID: ").append(goal.getId()).append("\n\n");
            }

            return response.toString();
        } catch (Exception e) {
            System.out.println("=== Error in GoalDescriptionTool: " + e.getMessage());
            e.printStackTrace();
            return "Error retrieving goals: " + e.getMessage();
        }
    }

    @Override
    public String getDescription() {
        return "This tool queries the user's goals and returns a list of the goals names and descriptions. " +
                "Uses the current user context automatically.";
    }

    @Override
    public ToolParameters getParameters() {
        return ToolParameters.builder()
                .type("object")
                .properties(Map.of(
                // No parameters needed - userId comes from UserContext
                ))
                .required(Arrays.asList())
                .build();
    }
}
