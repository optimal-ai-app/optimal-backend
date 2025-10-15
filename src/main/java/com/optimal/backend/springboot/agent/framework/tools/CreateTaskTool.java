package com.optimal.backend.springboot.agent.framework.tools;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.optimal.backend.springboot.agent.framework.core.UserContext;
import com.optimal.backend.springboot.database.entity.Goal;
import com.optimal.backend.springboot.database.entity.Task;
import com.optimal.backend.springboot.service.GoalService;
import com.optimal.backend.springboot.service.TaskService;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

@Component
public class CreateTaskTool {
    @Autowired
    private GoalService goalService;
    @Autowired
    private TaskService taskService;

    @Tool("Creates a task for a user's goal, repeating on specified days and times. Tasks will be scheduled starting from today. Uses the current user context automatically. Requires goalName (nullable), taskType, repeatEndDate, repeatDays, and dueTime.")
    public String CreateTask(
            @P("goalName") String goalName,
            @P("taskType") String taskType,
            @P("taskDescription") String taskDescription,
            @P("milestone") Boolean milestone,
            @P("value") Double value,
            @P("repeatEndDate") String repeatEndDate,
            @P("repeatDays") String[] repeatDays,
            @P("dueTime") String dueTime,
            @P("priority") String priority) {
        try {
            UUID userId = UserContext.requireUserId();

            boolean milestoneValue = milestone != null ? milestone : false;

            Timestamp repeatEndDateTimestamp = parseRepeatEndDate(repeatEndDate);
            List<String> repeatDaysList = repeatDays != null ? Arrays.asList(repeatDays) : null;

            Timestamp dueTimeTimestamp = dueTime != null && !dueTime.trim().isEmpty()
                    ? Timestamp.valueOf(parseDateTime(dueTime.trim()))
                    : null;
            String priorityValue = priority != null ? priority : "!!";

            UUID goalId = null;
            Goal goal = null;
            if (goalName != null) {
                Optional<Goal> goalOpt = (Optional<Goal>) goalService.getGoalByUserIdAndTitle(userId, goalName);
                if (goalOpt.isEmpty())
                    return "Goal not found for user.";
                goal = goalOpt.get();
                goalId = goal.getId();
                if (repeatEndDateTimestamp == null && repeatDaysList != null && !repeatDaysList.isEmpty()
                        && goal.getDueDate() != null)
                    repeatEndDateTimestamp = goal.getDueDate();
                if (repeatEndDateTimestamp != null && goal.getDueDate() != null
                        && repeatEndDateTimestamp.after(goal.getDueDate()))
                    repeatEndDateTimestamp = goal.getDueDate();
            }

            Task task = new Task();
            task.setUserId(userId);
            task.setMilestone(milestoneValue);
            task.setValue(value);
            task.setGoalId(goalId);
            task.setTitle(taskType != null ? taskType : "Task");
            task.setDescription(taskDescription != null ? taskDescription : "Task");
            task.setDueDate(dueTimeTimestamp);
            task.setPriority(priorityValue);
            task.setStatus("todo");

            Task created = taskService.createTask(task, repeatEndDateTimestamp, repeatDaysList);
            if (created == null)
                return "Task creation failed.";

            String message = "Task '" + task.getTitle() + "' created successfully with due date: "
                    + (dueTimeTimestamp != null ? dueTimeTimestamp : "not specified");
            if (repeatEndDateTimestamp != null && repeatDaysList != null && !repeatDaysList.isEmpty()) {
                message += ". Task will repeat on " + String.join(", ", repeatDaysList) + " until "
                        + repeatEndDateTimestamp;
                if (goal != null && goal.getDueDate() != null && repeatEndDateTimestamp.equals(goal.getDueDate()))
                    message += " (capped to goal completion date)";
            }
            return message;
        } catch (IllegalArgumentException e) {
            return "Error creating task: " + e.getMessage();
        }
    }

    /**
     * Parse various datetime input formats and ensure the date is today or in the
     * future.
     * For recurring tasks, this sets the initial date that will be used by
     * TaskService
     * to calculate proper repeat dates based on repeatDays.
     */
    private LocalDateTime parseDateTime(String timeInput) {
        // Use user's local date from context (their timezone)
        LocalDate today = UserContext.getUserLocalDate();
        LocalDateTime now = LocalDateTime.of(today, LocalTime.now());
        try {
            if (timeInput.contains(" ") && timeInput.length() >= 16) {
                LocalDateTime parsed = LocalDateTime.parse(timeInput,
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                return parsed.toLocalDate().isBefore(today) ? LocalDateTime.of(today, parsed.toLocalTime()) : parsed;
            }
            if (timeInput.matches("\\d{1,2}:\\d{2}(:\\d{2})?")) {
                LocalTime time = LocalTime.parse(timeInput,
                        DateTimeFormatter.ofPattern(timeInput.length() <= 5 ? "H:mm" : "H:mm:ss"));
                return LocalDateTime.of(today, time);
            }
            return now.plusHours(1);
        } catch (DateTimeParseException e) {
            return now.plusHours(1);
        }
    }

    private Timestamp parseRepeatEndDate(String input) {
        if (input == null || input.isBlank())
            return null;
        try {
            if (input.contains("T"))
                return Timestamp.valueOf(LocalDateTime.parse(input));
            if (input.matches("\\d{4}-\\d{2}-\\d{2}"))
                return Timestamp.valueOf(LocalDate.parse(input).atTime(23, 59, 59));
            return null;
        } catch (DateTimeParseException e) {
            return null;
        }
    }

}