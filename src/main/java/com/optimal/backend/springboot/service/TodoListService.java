package com.optimal.backend.springboot.service;

import com.optimal.backend.springboot.domain.entity.TodoList;
import com.optimal.backend.springboot.domain.repository.TodoListRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TodoListService {

    private final TodoListRepository todoListRepository;

    public List<TodoList> getAllTodoLists() {
        return todoListRepository.findAll();
    }

    public Optional<TodoList> getTodoListById(UUID id) {
        return todoListRepository.findById(id);
    }

    public TodoList createTodoList(TodoList todoList) {
        return todoListRepository.save(todoList);
    }

    public TodoList updateTodoList(TodoList todoList) {
        return todoListRepository.save(todoList);
    }

    public void deleteTodoList(UUID id) {
        todoListRepository.deleteById(id);
    }

    public List<TodoList> getTodoListsByUserId(UUID userId) {
        return todoListRepository.findByUserId(userId);
    }
}
