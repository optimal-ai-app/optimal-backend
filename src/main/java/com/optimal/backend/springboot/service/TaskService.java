package com.optimal.backend.springboot.service;

import com.optimal.backend.springboot.domain.entity.Task;
import com.optimal.backend.springboot.domain.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    public Optional<Task> getTaskById(UUID id) {
        return taskRepository.findById(id);
    }

    public Task createTask(Task task) {
        return taskRepository.save(task);
    }

    public Task updateTask(Task task) {
        return taskRepository.save(task);
    }

    public void deleteTask(UUID id) {
        taskRepository.deleteById(id);
    }

    public List<Task> getTasksByTodoListId(UUID todoListId) {
        return taskRepository.findByTodoListId(todoListId);
    }

    public List<Task> getTasksByUserId(UUID userId) {
        return taskRepository.findByUserId(userId);
    }
}
