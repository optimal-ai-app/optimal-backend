package com.optimal.backend.springboot.agent.framework.tools;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.optimal.backend.springboot.agent.framework.core.Tool;
import com.optimal.backend.springboot.agent.framework.core.UserContext;
import com.optimal.backend.springboot.database.entity.Goal;
import com.optimal.backend.springboot.database.entity.Task;
import com.optimal.backend.springboot.service.GoalService;
import com.optimal.backend.springboot.service.TaskService;

import dev.langchain4j.agent.tool.ToolParameters;

@Component
public class CreateTaskTool implements Tool {
    @Autowired
    private GoalService goalService;
    @Autowired
    private TaskService taskService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String getName() {
        return "createTaskForGoal";
    }

    @Override
    public String execute(String input) {
        try {
            UUID userId = UserContext.requireUserId();
            JsonNode in = objectMapper.readTree(input);

            String goalName = textOrNull(in, "goalName");
            String taskType = textOrNull(in, "taskType");
            String taskDescription = textOrNull(in, "taskDescription");
            boolean milestone = in.has("milestone") && !in.get("milestone").isNull() && in.get("milestone").asBoolean();
            JsonNode valueNode = in.get("value");
            Double value = valueNode != null && !valueNode.isNull() ? valueNode.asDouble() : null;

            Timestamp repeatEndDate = parseRepeatEndDate(textOrNull(in, "repeatEndDate"));
            List<String> repeatDays = (in.has("repeatDays") && in.get("repeatDays").isArray())
                    ? objectMapper.convertValue(in.get("repeatDays"), List.class)
                    : null;

            Timestamp dueTime = textOrNull(in, "dueTime") != null
                    ? Timestamp.valueOf(parseDateTime(textOrNull(in, "dueTime").trim()))
                    : null;
            String priority = Optional.ofNullable(textOrNull(in, "priority")).orElse("!!");

            UUID goalId = null;
            Goal goal = null;
            if (goalName != null) {
                Optional<Goal> goalOpt = (Optional<Goal>) goalService.getGoalByUserIdAndTitle(userId, goalName);
                if (goalOpt.isEmpty())
                    return "Goal not found for user.";
                goal = goalOpt.get();
                goalId = goal.getId();
                if (repeatEndDate == null && repeatDays != null && !repeatDays.isEmpty() && goal.getDueDate() != null)
                    repeatEndDate = goal.getDueDate();
                if (repeatEndDate != null && goal.getDueDate() != null && repeatEndDate.after(goal.getDueDate()))
                    repeatEndDate = goal.getDueDate();
            }

            Task task = new Task();
            task.setUserId(userId);
            task.setMilestone(milestone);
            task.setValue(value);
            task.setGoalId(goalId);
            task.setTitle(taskType != null ? taskType : "Task");
            task.setDescription(taskDescription != null ? taskDescription : "Task");
            task.setDueDate(dueTime);
            task.setPriority(priority);
            task.setStatus("todo");

            Task created = taskService.createTask(task, repeatEndDate, repeatDays);
            if (created == null)
                return "Task creation failed.";

            String message = "Task '" + task.getTitle() + "' created successfully with due date: "
                    + (dueTime != null ? dueTime : "not specified");
            if (repeatEndDate != null && repeatDays != null && !repeatDays.isEmpty()) {
                message += ". Task will repeat on " + String.join(", ", repeatDays) + " until " + repeatEndDate;
                if (goal != null && goal.getDueDate() != null && repeatEndDate.equals(goal.getDueDate()))
                    message += " (capped to goal completion date)";
            }
            return message;
        } catch (JsonProcessingException | IllegalArgumentException e) {
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
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();
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

    private String textOrNull(JsonNode node, String key) {
        return (node.has(key) && !node.get(key).isNull()) ? node.get(key).asText() : null;
    }

    @Override
    public String getDescription() {
        return "Creates a task for a user's goal, repeating on specified days and times. Tasks will be scheduled starting from today. Uses the current user context automatically. Requires goalName (nullable), taskType, repeatEndDate, repeatDays, and dueTime.";
    }

    @Override
    public ToolParameters getParameters() {
        return ToolParameters.builder().type("object").properties(Map.of(
                "goalName", Map.of("type", "string", "description", "The name of the goal (nullable)"),
                "taskType", Map.of("type", "string", "description", "The type or title of the task"),
                "taskDescription", Map.of("type", "string", "description", "The description of the task"),
                "repeatEndDate",
                Map.of("type", "string", "description",
                        "End date for the task. Can be ISO date (yyyy-MM-dd) or custom format (e.g. '2024-03-15'). Tasks will be scheduled for today or the future, never in the past."),
                "repeatDays",
                Map.of("type", "array", "items", Map.of("type", "string"), "description",
                        "List of days to repeat (e.g. Monday, Tuesday)"),
                "dueTime",
                Map.of("type", "string", "description",
                        "Time for the task. Can be HH:mm (e.g. '14:30') or full datetime (yyyy-MM-dd HH:mm:ss). Tasks will be scheduled for today or the future, never in the past."),
                "priority", Map.of("type", "string", "description", "The priority of the task (!!!, !!, !)")))
                .required(Arrays.asList("taskType", "repeatEndDate", "repeatDays", "dueTime", "priority")).build();
    }
}