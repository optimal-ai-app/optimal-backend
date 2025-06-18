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

    public List<Task> getTasksByUserIdAndGoalId(UUID userId, UUID goalId) {
        return taskRepository.findByUserIdAndGoalId(userId, goalId);
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

        // For recurring tasks, ensure the first task starts on the correct day
        if (!repeatDays.isEmpty()) {
            String targetDay = repeatDays.get(0);
            Timestamp adjustedDate = calculateDueDateForDay(task.getDueDate(), targetDay);
            task.setDueDate(adjustedDate);
        }

        // Save the first task
        Task firstTask = taskRepository.save(task);

        // Create the remaining repeated tasks
        Timestamp currentDate = firstTask.getDueDate();

        for (int i = 1; i < repeat; i++) {
            // Get the day for this repetition (cycles through repeatDays)
            String dayForThisRepeat = repeatDays.get((i - 1) % repeatDays.size());

            // Calculate the next occurrence after currentDate
            Timestamp nextDate = calculateDueDateForDay(currentDate, dayForThisRepeat);
            currentDate = nextDate;

            // Create and save the repeated task
            Task repeatedTask = new Task();
            repeatedTask.setTitle(task.getTitle());
            repeatedTask.setDescription(task.getDescription());
            repeatedTask.setPriority(task.getPriority());
            repeatedTask.setStatus(task.getStatus());
            repeatedTask.setUserId(task.getUserId());
            repeatedTask.setGoalId(task.getGoalId());
            repeatedTask.setDueDate(nextDate);

            taskRepository.save(repeatedTask);
        }

        return firstTask;
    }

    private Timestamp calculateDueDateForDay(Timestamp originalDueDate, String dayOfWeek) {
        Calendar calendar = Calendar.getInstance();
        if (originalDueDate != null) {
            calendar.setTime(originalDueDate);
        }

        System.out.println("=== calculateDueDateForDay Debug:");
        System.out.println("Input date: " + originalDueDate);
        System.out.println("Target day: " + dayOfWeek);

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

        System.out.println("Current day of week: " + currentDayOfWeek + ", Target: " + targetDayOfWeek
                + ", Days to add: " + daysToAdd);

        calendar.add(Calendar.DAY_OF_MONTH, daysToAdd);
        Timestamp result = new Timestamp(calendar.getTimeInMillis());

        System.out.println("Result date: " + result);

        return result;
    }

    public Task updateTask(Task task) {
        return taskRepository.save(task);
    }

    public void deleteTask(UUID id) {
        taskRepository.deleteById(id);
    }

}
