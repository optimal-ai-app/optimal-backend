package com.optimal.backend.springboot.agent.framework.tools;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.optimal.backend.springboot.database.entity.GoalProgress;
import com.optimal.backend.springboot.service.GoalProgressService;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

@Component
public class GetGoalProgressTool {

    @Autowired
    private GoalProgressService goalProgressService;

    @Tool("Get the goal progress")
    public String GetGoalProgress(@P("goalId") String goalId) {

        UUID goalIdUUID = UUID.fromString(goalId);
        List<GoalProgress> goalProgress = goalProgressService.getGoalProgressByGoalId(goalIdUUID);
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

    }
}
