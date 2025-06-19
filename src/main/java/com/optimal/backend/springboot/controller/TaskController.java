// src/main/java/com/optimal/backend/springboot/controller/TaskController.java
package com.optimal.backend.springboot.controller;

import com.optimal.backend.springboot.controller.RequestClasses.CreateTaskRequest;
import com.optimal.backend.springboot.domain.entity.Task;
import com.optimal.backend.springboot.service.TaskService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Task>> getTasksByUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(taskService.getTasksByUserId(userId));
    }

    @PostMapping("/create")
    public ResponseEntity<Task> createTask(@RequestBody CreateTaskRequest request) {
        Task task = new Task();
        task.setUserId(request.getUserId());
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setDueDate(request.getDueDate());
        task.setStatus("todo");
        task.setPriority(request.getPriority());
        task.setGoalId(request.getGoalId());
        return ResponseEntity.ok(
                taskService.createTask(task, request.getRepeatEndDate(), request.getRepeatDays()));
    }

}
