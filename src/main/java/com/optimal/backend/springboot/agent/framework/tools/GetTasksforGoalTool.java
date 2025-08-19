package com.optimal.backend.springboot.agent.framework.tools;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.optimal.backend.springboot.agent.framework.core.UserContext;
import com.optimal.backend.springboot.database.entity.Task;
import com.optimal.backend.springboot.service.TaskService;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

@Component
public class GetTasksforGoalTool {

    @Autowired
    private TaskService taskService;

    @Tool("Gets existing tasks for a specific goal by goal title. Uses the current user context automatically. " +
            "Requires only the goalTitle parameter.")
    public String GetTasksforGoal(@P("goalTitle") String goalTitle) {
        try {
            // Get userId from UserContext instead of parameters
            UUID userId = UserContext.requireUserId();
            System.out.println("=== GetTasksforGoalTool: Using userId from context: " + userId);
            System.out.println("=== GetTasksforGoalTool: Received goalTitle string: '" + goalTitle + "'");

            List<Task> tasks = taskService.getTasksByUserIdAndGoalTitle(userId, goalTitle);

            if (tasks.isEmpty()) {
                return "No tasks found for this goal.";
            }

            StringBuilder response = new StringBuilder();
            HashSet<UUID> sharedUUIDs = new HashSet<>();

            response.append("Here are the existing tasks for this goal, note that some tasks may be repeating:\n\n");
            for (int i = 0; i < tasks.size(); i++) {
                Task task = tasks.get(i);
                if (sharedUUIDs.contains(task.getSharedId())) {
                    continue;
                }
                sharedUUIDs.add(task.getSharedId()); // Add the shared UUID to the set
                response.append("**Task ").append(i + 1).append(":**\n");
                response.append("- Task ID: ").append(task.getId()).append("\n");
                response.append("- Shared ID: ").append(task.getSharedId()).append("\n");
                response.append("- Title: ").append(task.getTitle()).append("\n");
                response.append("- Description: ").append(task.getDescription()).append("\n");
                response.append("- Due Date: ").append(task.getDueDate()).append("\n");
                response.append("- Status: ").append(task.getStatus()).append("\n\n");
            }
            return response.toString();
        } catch (Exception e) {
            System.out.println("=== Error in GetTasksforGoalTool: " + e.getMessage());
            e.printStackTrace();
            return "Error retrieving tasks: " + e.getMessage();
        }
    }

}
