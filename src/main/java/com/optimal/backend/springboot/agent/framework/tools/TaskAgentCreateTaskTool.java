package com.optimal.backend.springboot.agent.framework.tools;

import com.optimal.backend.springboot.agent.framework.core.Tool;
import com.optimal.backend.springboot.agent.framework.core.UserContext;
import com.optimal.backend.springboot.service.GoalService;
import com.optimal.backend.springboot.service.TaskService;
import com.optimal.backend.springboot.domain.entity.Task;
import com.optimal.backend.springboot.domain.entity.Goal;

import dev.langchain4j.agent.tool.ToolParameters;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Arrays;
import java.util.Optional;

@Component
public class TaskAgentCreateTaskTool implements Tool {
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
            System.out.println("=== TaskAgentCreateTaskTool Input: " + input);
            System.out.println("=== Input raw bytes: " + java.util.Arrays.toString(input.getBytes()));

            // Get userId from UserContext instead of parameters
            UUID userId = UserContext.requireUserId();
            System.out.println("=== Using userId from UserContext: " + userId);

            JsonNode inputNode = objectMapper.readTree(input);
            String goalName = inputNode.has("goalName") && !inputNode.get("goalName").isNull()
                    ? inputNode.get("goalName").asText()
                    : null;
            String taskType = inputNode.has("taskType") && !inputNode.get("taskType").isNull()
                    ? inputNode.get("taskType").asText()
                    : null;
            String taskDescription = inputNode.has("taskDescription") && !inputNode.get("taskDescription").isNull()
                    ? inputNode.get("taskDescription").asText()
                    : null;
            Timestamp repeatEndDate = null;
            if (inputNode.has("repeatEndDate") && !inputNode.get("repeatEndDate").isNull()) {
                String endDateInput = inputNode.get("repeatEndDate").asText().trim();
                try {
                    // Try to parse as ISO date or custom format
                    LocalDateTime endDateTime;
                    if (endDateInput.contains("T")) {
                        endDateTime = LocalDateTime.parse(endDateInput);
                    } else if (endDateInput.matches("\\d{4}-\\d{2}-\\d{2}")) {
                        endDateTime = LocalDate.parse(endDateInput).atTime(23, 59, 59);
                    } else {
                        // Default to goal end date if parsing fails
                        endDateTime = null;
                    }
                    if (endDateTime != null) {
                        repeatEndDate = Timestamp.valueOf(endDateTime);
                    }
                } catch (DateTimeParseException e) {
                    System.out.println("Failed to parse repeatEndDate: " + endDateInput);
                    // Will default to goal end date below
                }
            }
            List<String> repeatDays = inputNode.has("repeatDays") && inputNode.get("repeatDays").isArray()
                    ? objectMapper.convertValue(inputNode.get("repeatDays"), List.class)
                    : null;

            System.out.println("=== Parsed Parameters:");
            System.out.println("userId from context: " + userId);
            System.out.println("goalName: " + goalName);
            System.out.println("taskType: '" + taskType + "'");
            System.out.println("taskType length: " + (taskType != null ? taskType.length() : "null"));
            System.out.println("taskType raw bytes: "
                    + (taskType != null ? java.util.Arrays.toString(taskType.getBytes()) : "null"));
            System.out.println("taskDescription: '" + taskDescription + "'");
            System.out.println("repeatEndDate: " + repeatEndDate);
            System.out.println("repeatDays: " + repeatDays);

            // Enhanced date/time handling to ensure tasks start from today
            Timestamp dueTime = null;
            if (inputNode.has("dueTime") && !inputNode.get("dueTime").isNull()) {
                String timeInput = inputNode.get("dueTime").asText().trim();
                LocalDateTime taskDateTime = parseDateTime(timeInput);
                dueTime = Timestamp.valueOf(taskDateTime);

                System.out.println("Parsed due time: " + taskDateTime + " from input: " + timeInput);
            }

            String priority = inputNode.has("priority") && !inputNode.get("priority").isNull()
                    ? inputNode.get("priority").asText()
                    : "!!";

            UUID goalId = null;
            Goal goal = null;
            if (goalName != null && userId != null) {
                Optional<Goal> goalOpt = goalService.getGoalByUserIdAndTitle(userId, goalName);
                if (goalOpt.isPresent()) {
                    goal = goalOpt.get();
                    goalId = goal.getId();
                    System.out.println("Found goal: " + goal.getTitle() + " with ID: " + goalId);

                    // If no repeatEndDate specified and we have repeatDays, default to goal's end
                    // date
                    if (repeatEndDate == null && repeatDays != null && !repeatDays.isEmpty()
                            && goal.getDueDate() != null) {
                        repeatEndDate = goal.getDueDate();
                        System.out.println("Using goal end date as default repeat end date: " + repeatEndDate);
                    }

                    // Cap repeatEndDate to goal's due date if it extends beyond the goal
                    if (repeatEndDate != null && goal.getDueDate() != null && repeatEndDate.after(goal.getDueDate())) {
                        System.out.println("Capping repeatEndDate from " + repeatEndDate + " to goal due date: "
                                + goal.getDueDate());
                        repeatEndDate = goal.getDueDate();
                    }
                } else {
                    System.out.println("Goal not found for user " + userId + " with name: " + goalName);
                    return "Goal not found for user.";
                }
            }

            Task task = new Task();
            task.setUserId(userId);
            task.setGoalId(goalId);
            task.setTitle(taskType != null ? taskType : "Task");
            task.setDescription(taskDescription != null ? taskDescription : "Task");
            task.setDueDate(dueTime);
            task.setPriority(priority);
            task.setStatus("todo");

            System.out.println("=== Task Object Created:");
            System.out.println("Title: '" + task.getTitle() + "'");
            System.out.println("Title length: " + task.getTitle().length());
            System.out.println("Title raw bytes: " + java.util.Arrays.toString(task.getTitle().getBytes()));
            System.out.println("Description: '" + task.getDescription() + "'");
            System.out.println("UserId: " + task.getUserId());
            System.out.println("GoalId: " + task.getGoalId());

            Task created = taskService.createTask(task, repeatEndDate, repeatDays);
            if (created != null) {
                System.out.println("=== Task Created Successfully:");
                String message = "Task '" + task.getTitle() + "' created successfully with due date: " +
                        (dueTime != null ? dueTime.toString() : "not specified");
                if (repeatEndDate != null && repeatDays != null && !repeatDays.isEmpty()) {
                    message += ". Task will repeat on " + String.join(", ", repeatDays) + " until " + repeatEndDate;

                    // Check if repeatEndDate was capped to goal's due date
                    if (goal != null && goal.getDueDate() != null && repeatEndDate.equals(goal.getDueDate())) {
                        message += " (capped to goal completion date)";
                    }
                }
                return message;
            } else {
                return "Task creation failed.";
            }
        } catch (Exception e) {
            System.out.println("=== Error in TaskAgentCreateTaskTool: " + e.getMessage());
            e.printStackTrace();
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

        System.out.println("=== parseDateTime Debug:");
        System.out.println("Today: " + today);
        System.out.println("Now: " + now);
        System.out.println("Time input: " + timeInput);

        try {
            // Try to parse as full datetime first (yyyy-MM-dd HH:mm:ss)
            if (timeInput.contains(" ") && timeInput.length() >= 16) {
                LocalDateTime parsed = LocalDateTime.parse(timeInput,
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                // If the parsed date is in the past, move it to today
                if (parsed.toLocalDate().isBefore(today)) {
                    return LocalDateTime.of(today, parsed.toLocalTime());
                }
                return parsed;
            }

            // Try to parse as time only (HH:mm or HH:mm:ss)
            if (timeInput.matches("\\d{1,2}:\\d{2}(:\\d{2})?")) {
                LocalTime time = LocalTime.parse(timeInput, DateTimeFormatter.ofPattern(
                        timeInput.length() <= 5 ? "H:mm" : "H:mm:ss"));

                // For time-only input, always start from today
                // TaskService will handle moving to correct days based on repeatDays
                LocalDateTime todayWithTime = LocalDateTime.of(today, time);

                // If the time has already passed today, start from today anyway
                // Let TaskService handle finding the next valid occurrence
                System.out.println("=== parseDateTime Result: " + todayWithTime);
                return todayWithTime;
            }

            // If nothing else works, default to 1 hour from now
            System.out.println("Could not parse time input '" + timeInput + "', defaulting to 1 hour from now");
            return now.plusHours(1);

        } catch (DateTimeParseException e) {
            System.out.println("Failed to parse datetime '" + timeInput + "': " + e.getMessage() +
                    ", defaulting to 1 hour from now");
            return now.plusHours(1);
        }
    }

    @Override
    public String getDescription() {
        return "Creates a task for a user's goal, repeating on specified days and times. " +
                "Tasks will be scheduled starting from today. Uses the current user context automatically. " +
                "Requires goalName (nullable), taskType, repeatEndDate, repeatDays, and dueTime.";
    }

    @Override
    public ToolParameters getParameters() {
        return ToolParameters.builder()
                .type("object")
                .properties(Map.of(
                        "goalName", Map.of("type", "string", "description", "The name of the goal (nullable)"),
                        "taskType", Map.of("type", "string", "description", "The type or title of the task"),
                        "taskDescription", Map.of("type", "string", "description", "The description of the task"),
                        "repeatEndDate",
                        Map.of("type", "string", "description",
                                "End date for the task. Can be ISO date (yyyy-MM-dd) or custom format (e.g. '2024-03-15'). "
                                        +
                                        "Tasks will be scheduled for today or the future, never in the past."),
                        "repeatDays",
                        Map.of("type", "array", "items", Map.of("type", "string"), "description",
                                "List of days to repeat (e.g. Monday, Tuesday)"),
                        "dueTime",
                        Map.of("type", "string", "description",
                                "Time for the task. Can be HH:mm (e.g. '14:30') or full datetime (yyyy-MM-dd HH:mm:ss). "
                                        +
                                        "Tasks will be scheduled for today or the future, never in the past."),
                        "priority", Map.of("type", "string", "description", "The priority of the task (!!!, !!, !)")))
                .required(Arrays.asList("taskType", "repeatEndDate", "repeatDays", "dueTime", "priority"))
                .build();
    }
}