package com.optimal.backend.springboot.service;

import com.optimal.backend.springboot.domain.entity.Task;
import com.optimal.backend.springboot.domain.repository.TaskRepository;
import com.optimal.backend.springboot.domain.repository.TodoListRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.optimal.backend.springboot.domain.entity.TodoList;

@Service
@RequiredArgsConstructor
public class TaskService {

    @Autowired
    private final TaskRepository taskRepository;

    @Autowired
    private final TodoListRepository todoListRepository;
    
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    public Optional<Task> getTaskById(UUID id) {
        return taskRepository.findById(id);
    }

    public Task createTask(Task task) {
        return taskRepository.save(task);
    }

    public Task createTask(UUID userId, String name, String description, Timestamp dueDate) {

        List<TodoList> todoLists = todoListRepository.findByUserId(userId);
        System.out.println(todoLists);
        TodoList todoList = null;
        if (todoLists.isEmpty()) {
            todoList = new TodoList();
            todoList.setName("Default List");
            todoList.setUserId(userId);
            todoList = todoListRepository.save(todoList);
        } else {
            todoList = todoLists.get(0);
        }

        Task task = new Task();
        task.setName(name);
        task.setDescription(description);
        task.setDueDate(dueDate);
        task.setStatus("To-Do");
        task.setTodoListId(todoList.getTodoListId());
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

}
