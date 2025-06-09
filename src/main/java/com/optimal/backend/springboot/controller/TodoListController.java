package com.optimal.backend.springboot.controller;

import com.optimal.backend.springboot.domain.entity.ToDo;
import com.optimal.backend.springboot.domain.repository.ToDoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/todos")
public class ToDoController {

    private final ToDoRepository todoRepository;

    @GetMapping("/{userId}")
    public ResponseEntity<List<ToDo>> getToDosByUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(todoRepository.findByUserId(userId));
    }

    @PostMapping
    public ResponseEntity<ToDo> createToDo(@RequestBody ToDo todo) {
        todo.setId(UUID.randomUUID());
        return ResponseEntity.ok(todoRepository.save(todo));
    }
}
