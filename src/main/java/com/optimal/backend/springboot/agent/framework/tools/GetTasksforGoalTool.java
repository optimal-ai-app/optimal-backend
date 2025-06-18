package com.optimal.backend.springboot.agent.framework.tools;

import java.util.Arrays;
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
import com.optimal.backend.springboot.domain.entity.Task;
import com.optimal.backend.springboot.service.TaskService;

import dev.langchain4j.agent.tool.ToolParameters;

@Component
public class GetTasksforGoalTool implements Tool {

    @Autowired
    private TaskService taskService;

    @Override
    public String execute(String input) {
        JsonNode inputNode = null;
        try {
            inputNode = new ObjectMapper().readTree(input);
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        if (inputNode == null) {
            return "Error: Invalid input format";
        }
        UUID userId = UUID.fromString(inputNode.get("userId").asText());
        UUID goalId = UUID.fromString(inputNode.get("goalId").asText());
        List<Task> tasks = taskService.getTasksByUserIdAndGoalId(userId, goalId);
        StringBuilder response = new StringBuilder();
        response.append("Here are the tasks for the goal:\n\n");
        for (Task task : tasks) {
            response.append("Title: ").append(task.getTitle()).append("\n");
            response.append("Description: ").append(task.getDescription()).append("\n\n");
            response.append("Due Date: ").append(task.getDueDate()).append("\n\n");
        }
        return response.toString();
    }

    @Override
    public String getName() {
        return "getTasksforGoal";
    }

    @Override
    public String getDescription() {
        return "GetTasksforGoalTool";
    }

    @Override
    public ToolParameters getParameters() {
        return ToolParameters.builder()
                .type("object")
                .properties(Map.of("userId", Map.of("type", "string", "description", "The user's UUID"),
                        "goalId", Map.of("type", "string", "description", "The goal's ID")))
                .required(Arrays.asList("userId", "goalId"))
                .build();
    }
}
