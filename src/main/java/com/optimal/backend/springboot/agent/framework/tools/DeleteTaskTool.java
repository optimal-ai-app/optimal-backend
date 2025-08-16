package com.optimal.backend.springboot.agent.framework.tools;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.optimal.backend.springboot.service.TaskService;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DeleteTaskTool {

    private final TaskService taskService;

    @Tool("Deletes a task by its ID.")
    public String DeleteTask(@P("taskId") String taskId) {
        try {
            UUID taskIdUUID = UUID.fromString(taskId);
            taskService.deleteTask(taskIdUUID);
            return "Task " + taskId + " deleted successfully.";
        } catch (Exception e) {
            return "Error deleting task: " + e.getMessage();
        }
    }
}