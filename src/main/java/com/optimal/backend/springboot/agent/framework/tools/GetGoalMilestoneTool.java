package com.optimal.backend.springboot.agent.framework.tools;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.optimal.backend.springboot.database.entity.GoalProgress;
import com.optimal.backend.springboot.database.entity.GoalType;
import com.optimal.backend.springboot.database.entity.Task;
import com.optimal.backend.springboot.service.GoalProgressService;
import com.optimal.backend.springboot.service.TaskService;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

@Component
public class GetGoalMilestoneTool {

    @Autowired
    private GoalProgressService goalProgressService;

    @Autowired
    private TaskService taskService;

    @Tool("Given a goalId, returns milestone tasks.")
    public String GetGoalMilestone(@P("goalId") String goalId) {
        try {
            UUID goalIdUUID = UUID.fromString(goalId);

            List<GoalProgress> progressList = goalProgressService.getGoalProgressByGoalId(goalIdUUID);
            if (progressList == null || progressList.isEmpty()) {
                return "No goal progress found for this goal.";
            }

            List<Task> milestones = taskService.getMilestonesByGoalId(goalIdUUID);
            if (milestones == null || milestones.isEmpty()) {
                return "No milestone tasks found for this goal.";
            }

            StringBuilder sb = new StringBuilder();
            sb.append("Milestone tasks for goal ").append(goalId).append(":\n\n");
            for (Task t : milestones) {
                sb.append("- ").append(t.getTitle());
                if (t.getDueDate() != null) {
                    sb.append(" (due ").append(t.getDueDate()).append(")");
                }
                sb.append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}
