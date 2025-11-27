package com.optimal.backend.springboot.service;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.optimal.backend.springboot.controller.RequestClasses.UpdateTaskRequest;
import com.optimal.backend.springboot.database.entity.Task;
import com.optimal.backend.springboot.database.repository.TaskRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final GoalProgressService goalProgressService;

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    public List<Task> getTasksByUserId(UUID userId) {
        return taskRepository.findByUserId(userId);
    }

    public List<Task> getTasksByUserIdAndGoalTitle(UUID userId, String goalTitle) {
        return taskRepository.findByUserIdAndGoalTitle(userId, goalTitle);
    }

    public List<Task> getMilestonesByGoalId(UUID goalId) {
        return taskRepository.findMilestonesByGoalId(goalId);
    }

    public List<Task> getUserTasksBeforeDueDate(UUID userId, Timestamp dueDate) {
        return taskRepository.findUserTasksBeforeDueDate(userId, dueDate);
    }

    public Optional<Task> getTaskById(UUID id) {
        return taskRepository.findById(id);
    }

    public void addTaskToProgress(Task task) {
        goalProgressService.addTaskToProgress(task);
    }

    public Task createTask(Task task) {
        if (task.getMilestone()) {
            addTaskToProgress(task);
        }
        return taskRepository.save(task);
    }

    public void updateTasks(List<String> tasks) {
        taskRepository.updateTasks(tasks);
    }

    public String getTasksForWeek(UUID userId, Date startDate, Date endDate) {
        List<Task> tasks = taskRepository.findByUserIdAndDueDateBetween(userId, startDate, endDate);
        StringBuilder tasksString = new StringBuilder();
        for (Task task : tasks) {
            tasksString.append(task.getTitle()).append(" - ").append(task.getStatus()).append(" - ")
                    .append(task.getDueDate()).append("\n");
        }
        return tasksString.toString();
    }

    @Transactional
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

        UUID sharedId = UUID.randomUUID();
        List<Task> tasksToSave = new ArrayList<>();

        while (!calendar.getTime().after(repeatEndDate)) {
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            String dayName = getDayNameFromCalendar(dayOfWeek);
            String dayAbbrev = getDayAbbreviationFromCalendar(dayOfWeek);

            if (repeatDays.stream().anyMatch(d -> d.equalsIgnoreCase(dayName) || d.equalsIgnoreCase(dayAbbrev))) {
                Task repeatedTask = createTaskCopy(task, new Timestamp(calendar.getTimeInMillis()), sharedId);
                tasksToSave.add(repeatedTask);
            }
            calendar.add(Calendar.DAY_OF_MONTH, 1); // Move to next day
        }

        if (!tasksToSave.isEmpty()) {
            List<Task> savedTasks = taskRepository.saveAll(tasksToSave);
            return savedTasks.get(0);
        }
        return taskRepository.save(task);
    }

    private Task createTaskCopy(Task originalTask, Timestamp dueDate, UUID sharedId) {
        Task taskCopy = new Task();
        taskCopy.setTitle(originalTask.getTitle());
        taskCopy.setDescription(originalTask.getDescription());
        taskCopy.setPriority(originalTask.getPriority());
        taskCopy.setStatus(originalTask.getStatus());
        taskCopy.setUserId(originalTask.getUserId());
        taskCopy.setGoalId(originalTask.getGoalId());
        taskCopy.setValue(originalTask.getValue());
        taskCopy.setMilestone(originalTask.getMilestone());
        taskCopy.setDueDate(dueDate);
        taskCopy.setSharedId(sharedId);
        return taskCopy;
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

    // TODO: move to utils
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

    public Task updateTask(UpdateTaskRequest request) {
        Task task = taskRepository.findById(request.getTaskId())
                .orElseThrow(() -> new RuntimeException("Task not found"));
        task.setStatus(request.getStatus());
        if (request.getCompletionDate() != null && !request.getCompletionDate().isEmpty()) {
            // Convert ISO timestamp to SQL timestamp format
            String isoTimestamp = request.getCompletionDate().replace("T", " ").replace("Z", "");
            task.setCompletedDate(Timestamp.valueOf(isoTimestamp));
        } else {
            task.setCompletedDate(null);
        }
        String isoTimestamp = request.getUpdatedAt().replace("T", " ").replace("Z", "");
        task.setUpdatedAt(Timestamp.valueOf(isoTimestamp));
        return updateTask(task);
    }

    @Transactional
    public void deleteTask(UUID id) {
        taskRepository.deleteById(id);
    }

    @Transactional
    public void deleteAllRelatedTasks(UUID sharedId) {
        taskRepository.deleteAllBySharedId(sharedId);
    }

    @Transactional
    public void deleteTaskAndAfter(UUID taskId) {
        System.out.println("--------------------------------");
        System.out.println("taskId: " + taskId);
        System.out.println("--------------------------------");
        taskRepository.deleteTaskAndAfter(taskId);
    }

}
