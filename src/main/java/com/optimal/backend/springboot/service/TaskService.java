package com.optimal.backend.springboot.service;

import com.optimal.backend.springboot.domain.entity.Task;
import com.optimal.backend.springboot.domain.repository.TaskRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.Calendar;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    public List<Task> getTasksByUserId(UUID userId) {
        return taskRepository.findByUserId(userId);
    }

    public Optional<Task> getTaskById(UUID id) {
        return taskRepository.findById(id);
    }

    public Task createTask(Task task) {
        return taskRepository.save(task);
    }

    public Task createTask(Task task, Integer repeat, List<String> repeatDays) {
        if (repeat == null || repeat <= 0 || repeatDays == null || repeatDays.isEmpty()) {
            return taskRepository.save(task);
        }

        // save the first task
        Task firstTask = taskRepository.save(task);

        // start from the first task's due date
        Timestamp lastDueDate = firstTask.getDueDate();
        int totalCopies = repeat - 1;

        for (int i = 0; i < totalCopies; i++) {
            // choose the correct weekday in cycle
            String currentDay = repeatDays.get(i % repeatDays.size());

            // compute the next occurrence *after* lastDueDate
            Timestamp newDueDate = calculateDueDateForDay(lastDueDate, currentDay);
            lastDueDate = newDueDate; // move the “cursor” forward

            // build & save the repeat task
            Task repeatedTask = new Task();
            repeatedTask.setTitle(task.getTitle());
            repeatedTask.setDescription(task.getDescription());
            repeatedTask.setPriority(task.getPriority());
            repeatedTask.setStatus(task.getStatus());
            repeatedTask.setUserId(task.getUserId());
            repeatedTask.setGoalId(task.getGoalId());
            repeatedTask.setDueDate(newDueDate);

            taskRepository.save(repeatedTask);
        }

        return firstTask;
    }

    private Timestamp calculateDueDateForDay(Timestamp originalDueDate, String dayOfWeek) {
        Calendar calendar = Calendar.getInstance();
        if (originalDueDate != null) {
            calendar.setTime(originalDueDate);
        }

        // Map day names to Calendar day constants
        int targetDayOfWeek;
        switch (dayOfWeek.toLowerCase()) {
            case "monday":
                targetDayOfWeek = Calendar.MONDAY;
                break;
            case "tuesday":
                targetDayOfWeek = Calendar.TUESDAY;
                break;
            case "wednesday":
                targetDayOfWeek = Calendar.WEDNESDAY;
                break;
            case "thursday":
                targetDayOfWeek = Calendar.THURSDAY;
                break;
            case "friday":
                targetDayOfWeek = Calendar.FRIDAY;
                break;
            case "saturday":
                targetDayOfWeek = Calendar.SATURDAY;
                break;
            case "sunday":
                targetDayOfWeek = Calendar.SUNDAY;
                break;
            default:
                // If invalid day, use the original due date
                return originalDueDate;
        }

        // Find the next occurrence of the target day
        int currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        int daysToAdd = (targetDayOfWeek - currentDayOfWeek + 7) % 7;
        if (daysToAdd == 0) {
            daysToAdd = 7; // If it's the same day, move to next week
        }

        calendar.add(Calendar.DAY_OF_MONTH, daysToAdd);
        return new Timestamp(calendar.getTimeInMillis());
    }

    public Task updateTask(Task task) {
        return taskRepository.save(task);
    }

    public void deleteTask(UUID id) {
        taskRepository.deleteById(id);
    }

}
