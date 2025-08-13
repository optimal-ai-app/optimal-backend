package com.optimal.backend.springboot.agent.framework.tools;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;  
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.optimal.backend.springboot.agent.framework.core.Tool;
import com.optimal.backend.springboot.database.entity.GoalProgress;
import com.optimal.backend.springboot.service.GoalProgressService;

@Component
public class GetGoalProgressTool implements Tool {

    @Autowired
    private GoalProgressService goalProgressService;

    @Override
    public String execute(String input) {
        try {
            JsonNode inputNode = new ObjectMapper().readTree(input);
            UUID goalId = UUID.fromString(inputNode.get("goalId").asText());
            List<GoalProgress> goalProgress = goalProgressService.getGoalProgressByGoalId(goalId);
            StringBuilder response = new StringBuilder();
            response.append("Goal Progress:\n\n");
            for (GoalProgress gp : goalProgress) {
                response.append("Goal ID: ").append(gp.getGoalId()).append("\n");
                response.append("Goal Type: ").append(gp.getGoalType()).append("\n");
                response.append("Total Units: ").append(gp.getTotalUnits()).append("\n");
                response.append("Completed Units: ").append(gp.getCompletedUnits()).append("\n");
                response.append("Updated At: ").append(gp.getUpdatedAt()).append("\n\n");
            }
            return response.toString();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }

    @Override
    public String getName() {
        return "get_goal_progress";
    }

    @Override
    public String getDescription() {
        return "Get the goal progress";
    }
}
