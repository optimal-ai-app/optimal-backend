package com.optimal.backend.springboot.agent.framework.tools;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.optimal.backend.springboot.agent.framework.core.Tool;
import com.optimal.backend.springboot.agent.framework.core.UserContext;
import com.optimal.backend.springboot.domain.entity.Task;
import com.optimal.backend.springboot.service.TaskService;

import dev.langchain4j.agent.tool.ToolParameters;

@Component
public class GetTasksforGoalTool implements Tool {

    @Autowired
    private TaskService taskService;

    @Override
    public String execute(String input) {
        try {
            // Get userId from UserContext instead of parameters
            UUID userId = UserContext.requireUserId();
            System.out.println("=== GetTasksforGoalTool: Using userId from context: " + userId);

            JsonNode inputNode = new ObjectMapper().readTree(input);
            String goalTitle = inputNode.get("goalTitle").asText();

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
                response.append("- Shared ID: ").append(task.getSharedId()).append("\n");
                response.append("- Title: ").append(task.getTitle()).append("\n");
                response.append("- Description: ").append(task.getDescription()).append("\n");
                response.append("- Due Date: ").append(task.getDueDate()).append("\n");
                response.append("- Status: ").append(task.getStatus()).append("\n\n");
            }
            return response.toString();
        } catch (JsonMappingException e) {
            System.out.println("=== Error in GetTasksforGoalTool (JsonMapping): " + e.getMessage());
            e.printStackTrace();
            return "Error: Invalid input format";
        } catch (JsonProcessingException e) {
            System.out.println("=== Error in GetTasksforGoalTool (JsonProcessing): " + e.getMessage());
            e.printStackTrace();
            return "Error: Invalid input format";
        } catch (Exception e) {
            System.out.println("=== Error in GetTasksforGoalTool: " + e.getMessage());
            e.printStackTrace();
            return "Error retrieving tasks: " + e.getMessage();
        }
    }

    @Override
    public String getName() {
        return "getTasksforGoal";
    }

    @Override
    public String getDescription() {
        return "Gets existing tasks for a specific goal by goal title. Uses the current user context automatically. " +
                "Requires only the goalTitle parameter.";
    }

    @Override
    public ToolParameters getParameters() {
        return ToolParameters.builder()
                .type("object")
                .properties(Map.of(
                        "goalTitle", Map.of("type", "string", "description", "The goal's title/name")))
                .required(Arrays.asList("goalTitle"))
                .build();
    }
}
