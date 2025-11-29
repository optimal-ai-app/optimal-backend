// src/main/java/com/optimal/backend/springboot/controller/TaskController.java
package com.optimal.backend.springboot.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.optimal.backend.springboot.controller.RequestClasses.CreateTaskRequest;
import com.optimal.backend.springboot.controller.RequestClasses.UpdateTaskRequest;
import com.optimal.backend.springboot.database.entity.Task;
import com.optimal.backend.springboot.security.annotation.CurrentUser;
import com.optimal.backend.springboot.security.model.TokenUserContext;
import com.optimal.backend.springboot.service.TaskService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Task>> getTasksByUser(@CurrentUser TokenUserContext userContext) {
        System.out.println("userContext: " + userContext.toString());
        return ResponseEntity.ok(taskService.getTasksByUserId(userContext.getUserId()));
    }

    @PostMapping("/create")
    public ResponseEntity<Task> createTask(@RequestBody CreateTaskRequest request,
            @CurrentUser TokenUserContext userContext) {
        Task task = new Task();
        task.setUserId(userContext.getUserId());
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setDueDate(request.getDueDate());
        task.setStatus("todo");
        task.setPriority(request.getPriority());
        task.setGoalId(request.getGoalId());
        task.setMilestone(request.getMilestone());

        System.out.println("--------------------------------");
        System.out.println("task: " + task.toString());
        System.out.println("request: " + request.getRepeatEndDate());
        System.out.println("request repeat days: " + String.join(", ",
                request.getRepeatDays() != null ? request.getRepeatDays().toArray(new String[0]) : new String[] {}));
        System.out.println("--------------------------------");

        return ResponseEntity.ok(
                taskService.createTask(task, request.getRepeatEndDate(), request.getRepeatDays()));
    }

    @PostMapping("/update")
    public ResponseEntity<Task> updateTask(@RequestBody UpdateTaskRequest request,
            @CurrentUser TokenUserContext userContext) {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode js = mapper.valueToTree(request);
        System.out.println(js.toPrettyString());
        return ResponseEntity.ok(taskService.updateTask(request));
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(@PathVariable UUID taskId, @CurrentUser TokenUserContext userContext) {
        taskService.deleteTask(taskId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/shared/{sharedId}")
    public ResponseEntity<Void> deleteAllRelatedTasks(@PathVariable UUID sharedId,
            @CurrentUser TokenUserContext userContext) {
        taskService.deleteAllRelatedTasks(sharedId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/after/{taskId}")
    public ResponseEntity<Void> deleteTaskAndAfter(@PathVariable UUID taskId,
            @CurrentUser TokenUserContext userContext) {

        taskService.deleteTaskAndAfter(taskId);
        return ResponseEntity.noContent().build();
    }
}
