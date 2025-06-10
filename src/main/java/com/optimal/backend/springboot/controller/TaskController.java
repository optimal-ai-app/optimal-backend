// src/main/java/com/optimal/backend/springboot/controller/TaskController.java
package com.optimal.backend.springboot.controller;

import com.optimal.backend.springboot.domain.entity.Task;
import com.optimal.backend.springboot.service.TaskService;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tasks")
public class TaskController {

    @Autowired
    private final TaskService taskService;
    
    @PostMapping("/create")
    public ResponseEntity<Task> createTask(@RequestBody CreateTaskRequest request) {
        return ResponseEntity.ok(taskService.createTask(request.getUserId(), request.getName(), request.getDescription(), request.getDueDate()));
    }

    public static class CreateTaskRequest {
        private UUID userId;
        private String name;
        private String description;
        private Timestamp dueDate;

        public UUID getUserId() { return userId; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public Timestamp getDueDate() { return dueDate; }
    }
    
    // @GetMapping("/user/{userId}")
    // public ResponseEntity<List<Task>> getTasksByUser(@PathVariable UUID userId) {
    //     return ResponseEntity.ok(taskService.getTasksByUserId(userId));
    // }

    @GetMapping("/todolist/{todoListId}")
    public ResponseEntity<List<Task>> getTasksByTodoList(@PathVariable UUID todoListId) {
        return ResponseEntity.ok(taskService.getTasksByTodoListId(todoListId));
    }
}
