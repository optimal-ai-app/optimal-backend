package com.optimal.backend.springboot.agent.framework.tools;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.optimal.backend.springboot.agent.framework.core.Tool;
import com.optimal.backend.springboot.database.entity.GoalProgress;
import com.optimal.backend.springboot.database.entity.GoalType;
import com.optimal.backend.springboot.database.entity.Task;
import com.optimal.backend.springboot.service.GoalProgressService;
import com.optimal.backend.springboot.service.TaskService;

@Component
public class GetGoalMilestoneTool implements Tool {

    @Autowired
    private GoalProgressService goalProgressService;

    @Autowired
    private TaskService taskService;

    @Override
    public String getName() {
        return "get_goal_milestones";
    }

    @Override
    public String getDescription() {
        return "Given a goalId, returns milestone tasks if the goal is QUALITATIVE; otherwise returns a notice that quantitative goals have no milestones.";
    }

    @Override
    public String execute(String input) {
        try {
            JsonNode node = new ObjectMapper().readTree(input);
            UUID goalId = UUID.fromString(node.get("goalId").asText());

            List<GoalProgress> progressList = goalProgressService.getGoalProgressByGoalId(goalId);
            if (progressList == null || progressList.isEmpty()) {
                return "No goal progress found for this goal.";
            }

            GoalProgress progress = progressList.get(0);
            if (progress.getGoalType() == GoalType.QUANTITATIVE) {
                return "This goal is quantitative; it has no milestones.";
            }

            List<Task> milestones = taskService.getMilestonesByGoalId(goalId);
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
