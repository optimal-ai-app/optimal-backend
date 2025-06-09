// src/main/java/com/optimal/backend/springboot/controller/TaskController.java
package com.optimal.backend.springboot.controller;

import com.optimal.backend.springboot.domain.entity.Task;
import com.optimal.backend.springboot.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Task>> getTasksByUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(taskService.getTasksByUser(userId));
    }

    @GetMapping("/todolist/{todoListId}")
    public ResponseEntity<List<Task>> getTasksByTodoList(@PathVariable UUID todoListId) {
        return ResponseEntity.ok(taskService.getTasksByTodoList(todoListId));
    }
}
