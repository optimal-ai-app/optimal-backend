package com.optimal.backend.springboot.agent.framework.tools;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.optimal.backend.springboot.agent.framework.core.UserContext;
import com.optimal.backend.springboot.controller.RequestClasses.CreateGoalRequest;
import com.optimal.backend.springboot.database.entity.Goal;
import com.optimal.backend.springboot.service.GoalService;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

@Component
public class CreateGoalTool {

    @Autowired
    private GoalService goalService;

    @Tool("Creates a goal for a user with a simple due date. " +
            "Uses the current user context automatically. " +
            "Requires goalTitle, goalDescription (optional), dueTime, and tags (optional).")
    public String CreateGoal(
            @P("goalTitle") String goalTitle,
            @P("goalDescription") String goalDescription,
            @P("dueTime") String dueTime,
            @P("tags") String[] tags) {
        try {
            // Get userId from UserContext instead of parameters
            UUID userId = UserContext.requireUserId();
            System.out.println("=== Using userId from UserContext: " + userId);

            // Truncate description to 300 characters if necessary
            if (goalDescription != null && goalDescription.length() > 300) {
                goalDescription = goalDescription.substring(0, 300);
            }

            System.out.println("=== Processing Parameters:");
            System.out.println("userId from context: " + userId);
            System.out.println("goalTitle: " + goalTitle);
            System.out.println("goalDescription: '" + goalDescription + "'");

            Timestamp dueDateTimestamp = null;
            if (dueTime != null && !dueTime.trim().isEmpty()) {
                dueDateTimestamp = parseGoalDueDate(dueTime.trim());
                System.out.println("Parsed due date: " + dueDateTimestamp + " from input: " + dueTime);

            }
            // Validate required fields
            if (goalTitle == null || goalTitle.trim().isEmpty()) {
                return "Goal title is required.";
            }

            if (dueDateTimestamp == null) {
                return "Goal due date is required.";
            }

            // Create the goal request
            CreateGoalRequest goalRequest = new CreateGoalRequest();
            goalRequest.setTitle(goalTitle);
            goalRequest.setDescription(goalDescription != null ? goalDescription : "");
            goalRequest.setDueDate(dueDateTimestamp);
            goalRequest.setTags(tags);

            Goal created = goalService.createGoal(goalRequest, UserContext.requireUserId());
            if (created != null) {
                System.out.println("=== Goal Created Successfully:");
                String message = "Goal '" + created.getTitle() + "' created successfully with due date: "
                        + dueDateTimestamp.toString();

                System.out.println("=== Goal Object Created:");
                System.out.println("Title: '" + created.getTitle() + "'");
                System.out.println("Description: '" + created.getDescription() + "'");
                System.out.println("UserId: " + created.getUserId());
                System.out.println("Due Date: " + created.getDueDate());
                return message;
            } else {
                return "Goal creation failed.";
            }
        } catch (Exception e) {
            System.out.println("=== Error in GoalAgentCreateGoalTool: " + e.getMessage());
            e.printStackTrace();
            return "Error creating goal: " + e.getMessage();
        }
    }

    /**
     * Parse goal due date from various date formats.
     * Supports ISO date format (yyyy-MM-dd) and other common formats.
     * Uses user's local date from context instead of server date.
     * If a date is in the past, finds the next occurrence of that date (adds years
     * until future).
     */
    private Timestamp parseGoalDueDate(String dateInput) {
        // Use user's local date from their time zone
        LocalDate today = UserContext.getUserLocalDate();

        try {
            LocalDate parsedDate;

            // Try ISO date format first (yyyy-MM-dd)
            if (dateInput.matches("\\d{4}-\\d{2}-\\d{2}")) {
                parsedDate = LocalDate.parse(dateInput);
            }
            // Try other common formats
            else if (dateInput.matches("\\d{2}/\\d{2}/\\d{4}")) {
                parsedDate = LocalDate.parse(dateInput, DateTimeFormatter.ofPattern("MM/dd/yyyy"));
            } else if (dateInput.matches("\\d{2}-\\d{2}-\\d{4}")) {
                parsedDate = LocalDate.parse(dateInput, DateTimeFormatter.ofPattern("MM-dd-yyyy"));
            } else {
                // Try to parse with default formatter
                parsedDate = LocalDate.parse(dateInput);
            }

            // If date is in the past, find the next occurrence (add years until future)
            if (parsedDate.isBefore(today)) {
                int month = parsedDate.getMonthValue();
                int day = parsedDate.getDayOfMonth();
                int currentYear = today.getYear();

                // Try next year first
                try {
                    parsedDate = LocalDate.of(currentYear + 1, month, day);
                } catch (Exception e) {
                    // Handle invalid date (e.g., Feb 29 in non-leap year) - use last valid day of
                    // month
                    parsedDate = LocalDate.of(currentYear + 1, month, 1).withDayOfMonth(
                            LocalDate.of(currentYear + 1, month, 1).lengthOfMonth());
                }

                // Keep adding years if still in the past (edge case)
                while (parsedDate.isBefore(today)) {
                    parsedDate = parsedDate.plusYears(1);
                }
            }

            // Convert to Timestamp (end of day)
            LocalDateTime endOfDay = parsedDate.atTime(23, 59, 59);
            Timestamp timestamp = Timestamp.valueOf(endOfDay);

            System.out.println("=== parseGoalDueDate Result: " + timestamp);
            return timestamp;

        } catch (DateTimeParseException e) {
            System.out.println("Failed to parse date '" + dateInput + "': " + e.getMessage() +
                    ", defaulting to 30 days from user's today");
            LocalDate defaultDate = today.plusDays(30);
            LocalDateTime endOfDay = defaultDate.atTime(23, 59, 59);
            return Timestamp.valueOf(endOfDay);
        }
    }
}