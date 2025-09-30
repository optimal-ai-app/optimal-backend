package com.optimal.backend.springboot.agent.framework.tools;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.optimal.backend.springboot.agent.framework.core.UserContext;
import com.optimal.backend.springboot.database.entity.Goal;
import com.optimal.backend.springboot.service.GoalService;

import dev.langchain4j.agent.tool.Tool;
import jakarta.annotation.PostConstruct;

@Component
public class GetGoalDescriptionTool {

    @Autowired
    private GoalService goalService;

    @Tool("This tool queries the user's goals and returns a list of the goals names and descriptions. " +
            "Uses the current user context automatically.")
    public String GetGoalDescription() {
        try {
            UUID userId = UserContext.requireUserId();
            System.out.println("=== GoalDescriptionTool: Using userId from context: " + userId);

            List<Goal> goals = goalService.getGoalsByUser(userId);

            if (goals.isEmpty()) {
                return "No goals found for this user.";
            }

            StringBuilder response = new StringBuilder();
            response.append("Here are the user's goals:\n\n");

            for (int i = 0; i < goals.size(); i++) {
                Goal goal = goals.get(i);
                response.append("**Goal ").append(i + 1).append(":**\n");
                response.append("- Title: ").append(goal.getTitle()).append("\n");
                response.append("- Description: ").append(goal.getDescription()).append("\n");
                response.append("- End Date: ").append(goal.getDueDate()).append("\n");
                response.append("- Status: ").append(goal.getStatus()).append("\n");
                response.append("- Goal ID: ").append(goal.getId()).append("\n\n");
            }
            System.out.println(response.toString());
            return response.toString();
        } catch (Exception e) {
            System.out.println("=== Error in GoalDescriptionTool: " + e.getMessage());
            e.printStackTrace();
            return "Error retrieving goals: " + e.getMessage();
        }
    }

    @PostConstruct
    protected void initialize() {
        if (goalService != null) {
            System.out.println("\nGoal Service Initialized\n");
        }
    }
}
