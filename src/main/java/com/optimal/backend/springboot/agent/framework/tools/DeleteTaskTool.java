package com.optimal.backend.springboot.agent.framework.tools;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.optimal.backend.springboot.service.TaskService;
import com.optimal.backend.springboot.agent.framework.core.Tool;
import dev.langchain4j.agent.tool.ToolParameters;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DeleteTaskTool implements Tool {

    private final TaskService taskService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String getName() {
        return "deleteTask";
    }

    @Override
    public String getDescription() {
        return "Deletes a task by its ID.";
    }

    @Override
    public ToolParameters getParameters() {
        return ToolParameters.builder()
                .type("object")
                .properties(Map.of(
                        "taskId", Map.of("type", "string", "description", "The ID of the task to delete.")))
                .build();
    }

    @Override
    public String execute(String jsonInput) {
        try {
            Map<String, Object> arguments = objectMapper.readValue(jsonInput, new TypeReference<>() {});
            String taskIdStr = (String) arguments.get("taskId");
            UUID taskId = UUID.fromString(taskIdStr);
            taskService.deleteTask(taskId);
            return "Task " + taskId + " deleted successfully.";
        } catch (Exception e) {
            return "Error deleting task: " + e.getMessage();
        }
    }
} 