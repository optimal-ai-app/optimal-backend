package com.optimal.backend.springboot.agent.framework.tools;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.optimal.backend.springboot.agent.framework.core.UserContext;
import com.optimal.backend.springboot.database.entity.Goal;
import com.optimal.backend.springboot.database.entity.GoalProgress;
import com.optimal.backend.springboot.database.entity.Task;
import com.optimal.backend.springboot.service.GoalProgressService;
import com.optimal.backend.springboot.service.GoalService;
import com.optimal.backend.springboot.service.TaskService;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

@Component
public class GetGoalMilestoneTool {

    @Autowired
    private GoalProgressService goalProgressService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private GoalService goalService;

    @Tool("Given a goalId (UUID) or goal title, returns milestone tasks.")
    public String GetGoalMilestone(@P("goalId") String goalIdOrTitle) {
        try {
            UUID goalIdUUID;
            try {
                goalIdUUID = UUID.fromString(goalIdOrTitle);
            } catch (IllegalArgumentException e) {
                // If not a valid UUID, try to find goal by title
                UUID userId = UserContext.requireUserId();
                List<Goal> userGoals = goalService.getGoalsByUser(userId);
                
                goalIdUUID = userGoals.stream()
                    .filter(g -> g.getTitle().equalsIgnoreCase(goalIdOrTitle))
                    .findFirst()
                    .map(Goal::getId)
                    .orElseThrow(() -> new IllegalArgumentException("Could not find goal with ID or Title: " + goalIdOrTitle));
            }

            List<GoalProgress> progressList = goalProgressService.getGoalProgressByGoalId(goalIdUUID);
            if (progressList == null || progressList.isEmpty()) {
                // Return explicitly if no progress, though we could check milestones directly too.
                // Keeping original logic structure but ensuring we proceed if milestones might exist.
                // Actually, let's just check milestones directly as well, or return message.
                // Original code returned "No goal progress found".
                // We'll proceed to check milestones if progress is empty, just in case.
                // But for now, let's keep it simple and consistent with original intent, but allow success if milestones found.
            }

            List<Task> milestones = taskService.getMilestonesByGoalId(goalIdUUID);
            if (milestones == null || milestones.isEmpty()) {
                return "No milestone tasks found for this goal.";
            }

            StringBuilder sb = new StringBuilder();
            sb.append("Milestone tasks for goal ").append(goalIdOrTitle).append(":\n\n");
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
