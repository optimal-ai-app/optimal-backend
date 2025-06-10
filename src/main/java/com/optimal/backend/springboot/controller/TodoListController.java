package com.optimal.backend.springboot.controller;

import com.optimal.backend.springboot.domain.entity.TodoList;
import com.optimal.backend.springboot.domain.repository.TodoListRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/todolists")
public class TodoListController {

    private final TodoListRepository todoListRepository;

    @GetMapping("/{userId}")
    public ResponseEntity<List<TodoList>> getTodoListsByUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(todoListRepository.findByUserId(userId));
    }

    @PostMapping
    public ResponseEntity<TodoList> createTodoList(@RequestBody TodoList todoList) {
        return ResponseEntity.ok(todoListRepository.save(todoList));
    }
}
