package com.optimal.backend.springboot.service;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.optimal.backend.springboot.domain.entity.Task;
import com.optimal.backend.springboot.domain.repository.TaskRepository;

import lombok.RequiredArgsConstructor;

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

    public List<Task> getTasksByUserIdAndGoalTitle(UUID userId, String goalTitle) {
        return taskRepository.findByUserIdAndGoalTitle(userId, goalTitle);
    }

    public Optional<Task> getTaskById(UUID id) {
        return taskRepository.findById(id);
    }

    public Task createTask(Task task) {
        return taskRepository.save(task);
    }

    public Task createTask(Task task, Timestamp repeatEndDate, List<String> repeatDays) {
        if (repeatEndDate == null || repeatDays == null || repeatDays.isEmpty()) {
            return taskRepository.save(task);
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new java.util.Date()); // Start from today

        Timestamp today = new Timestamp(calendar.getTimeInMillis());
        Timestamp startDate = task.getDueDate() != null && task.getDueDate().after(today)
                ? task.getDueDate()
                : today;

        calendar.setTime(startDate);

        Task firstTask = null;
        boolean firstTaskCreated = false;
        UUID sharedId = UUID.randomUUID();

        while (!calendar.getTime().after(repeatEndDate)) {
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            String dayName = getDayNameFromCalendar(dayOfWeek);
            String dayAbbrev = getDayAbbreviationFromCalendar(dayOfWeek);
            if (repeatDays.stream().anyMatch(d -> 
                d.equalsIgnoreCase(dayName) || d.equalsIgnoreCase(dayAbbrev))) {
                Task repeatedTask = new Task();
                repeatedTask.setTitle(task.getTitle());
                repeatedTask.setDescription(task.getDescription());
                repeatedTask.setPriority(task.getPriority());
                repeatedTask.setStatus(task.getStatus());
                repeatedTask.setUserId(task.getUserId());
                repeatedTask.setGoalId(task.getGoalId());
                repeatedTask.setDueDate(new Timestamp(calendar.getTimeInMillis()));
                repeatedTask.setSharedId(sharedId);
                if (!firstTaskCreated) {
                    firstTask = taskRepository.save(repeatedTask);
                    firstTaskCreated = true;
                } else {
                    taskRepository.save(repeatedTask);
                }
            }
            calendar.add(Calendar.DAY_OF_MONTH, 1); // Move to next day
        }

        return firstTask;
    }

    private String getDayNameFromCalendar(int dayOfWeek) {
        switch (dayOfWeek) {
            case Calendar.SUNDAY:
                return "sunday";
            case Calendar.MONDAY:
                return "monday";
            case Calendar.TUESDAY:
                return "tuesday";
            case Calendar.WEDNESDAY:
                return "wednesday";
            case Calendar.THURSDAY:
                return "thursday";
            case Calendar.FRIDAY:
                return "friday";
            case Calendar.SATURDAY:
                return "saturday";
            default:
                throw new IllegalArgumentException("Invalid day of week");
        }
    }

    private String getDayAbbreviationFromCalendar(int dayOfWeek) {
        switch (dayOfWeek) {
            case Calendar.SUNDAY:
                return "SU";
            case Calendar.MONDAY:
                return "M";
            case Calendar.TUESDAY:
                return "T";
            case Calendar.WEDNESDAY:
                return "W";
            case Calendar.THURSDAY:
                return "TH";
            case Calendar.FRIDAY:
                return "F";
            case Calendar.SATURDAY:
                return "S";
            default:
                throw new IllegalArgumentException("Invalid day of week");
        }
    }

    public Task updateTask(Task task) {
        return taskRepository.save(task);
    }

    public void deleteTask(UUID id) {
        taskRepository.deleteById(id);
    }

}
