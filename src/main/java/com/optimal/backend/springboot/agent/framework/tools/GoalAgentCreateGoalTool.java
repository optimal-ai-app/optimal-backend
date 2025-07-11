package com.optimal.backend.springboot.agent.framework.tools;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.optimal.backend.springboot.agent.framework.core.Tool;
import com.optimal.backend.springboot.agent.framework.core.UserContext;
import com.optimal.backend.springboot.controller.RequestClasses.CreateGoalRequest;
import com.optimal.backend.springboot.domain.entity.Goal;
import com.optimal.backend.springboot.service.GoalService;

import dev.langchain4j.agent.tool.ToolParameters;

@Component
public class GoalAgentCreateGoalTool implements Tool {
    @Autowired
    private GoalService goalService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String getName() {
        return "createGoal";
    }

    @Override
    public String execute(String input) {
        try {
            System.out.println("=== GoalAgentCreateGoalTool Input: " + input);
            // Get userId from UserContext instead of parameters
            UUID userId = UserContext.requireUserId();
            System.out.println("=== Using userId from UserContext: " + userId);

            JsonNode inputNode = objectMapper.readTree(input);
            String goalTitle = inputNode.has("goalTitle") && !inputNode.get("goalTitle").isNull()
                    ? inputNode.get("goalTitle").asText()
                    : null;
            String goalDescription = inputNode.has("goalDescription") && !inputNode.get("goalDescription").isNull()
                    ? inputNode.get("goalDescription").asText()
                    : null;
            
            // Parse tags (optional)
            String[] tags = null;
            if (inputNode.has("tags") && !inputNode.get("tags").isNull() && inputNode.get("tags").isArray()) {
                List<String> tagList = new ArrayList<>();
                for (JsonNode tagNode : inputNode.get("tags")) {
                    tagList.add(tagNode.asText());
                }
                tags = tagList.toArray(new String[0]);
            }
            
            System.out.println("=== Parsed Parameters:");
            System.out.println("userId from context: " + userId);
            System.out.println("goalTitle: " + goalTitle);
            System.out.println("goalDescription: '" + goalDescription + "'");

            // Simple due date parsing for goal completion
            Timestamp dueTime = null;
            if (inputNode.has("dueTime") && !inputNode.get("dueTime").isNull()) {
                String dateInput = inputNode.get("dueTime").asText().trim();
                dueTime = parseGoalDueDate(dateInput);
                System.out.println("Parsed due date: " + dueTime + " from input: " + dateInput);
            }
            // Validate required fields
            if (goalTitle == null || goalTitle.trim().isEmpty()) {
                return "Goal title is required.";
            }

            if (dueTime == null) {
                return "Goal due date is required.";
            }

            // Create the goal request
            CreateGoalRequest goalRequest = new CreateGoalRequest();
            goalRequest.setUserId(userId);
            goalRequest.setTitle(goalTitle);
            goalRequest.setDescription(goalDescription != null ? goalDescription : "");
            goalRequest.setDueDate(dueTime);
            goalRequest.setTags(tags);
            
            Goal created = goalService.createGoal(goalRequest);
            if (created != null) {
                System.out.println("=== Goal Created Successfully:");
                String message = "Goal '" + created.getTitle() + "' created successfully with due date: " + dueTime.toString();
                
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
     */
    private Timestamp parseGoalDueDate(String dateInput) {
        LocalDate today = LocalDate.now();
        
        System.out.println("=== parseGoalDueDate Debug:");
        System.out.println("Today: " + today);
        System.out.println("Date input: " + dateInput);

        try {
            LocalDate parsedDate;
            
            // Try ISO date format first (yyyy-MM-dd)
            if (dateInput.matches("\\d{4}-\\d{2}-\\d{2}")) {
                parsedDate = LocalDate.parse(dateInput);
            }
            // Try other common formats
            else if (dateInput.matches("\\d{2}/\\d{2}/\\d{4}")) {
                parsedDate = LocalDate.parse(dateInput, DateTimeFormatter.ofPattern("MM/dd/yyyy"));
            }
            else if (dateInput.matches("\\d{2}-\\d{2}-\\d{4}")) {
                parsedDate = LocalDate.parse(dateInput, DateTimeFormatter.ofPattern("MM-dd-yyyy"));
            }
            else {
                // Try to parse with default formatter
                parsedDate = LocalDate.parse(dateInput);
            }
            
            // Ensure the date is not in the past
            if (parsedDate.isBefore(today)) {
                System.out.println("Date " + parsedDate + " is in the past, using today instead");
                parsedDate = today;
            }
            
            // Convert to Timestamp (end of day)
            LocalDateTime endOfDay = parsedDate.atTime(23, 59, 59);
            Timestamp timestamp = Timestamp.valueOf(endOfDay);
            
            System.out.println("=== parseGoalDueDate Result: " + timestamp);
            return timestamp;

        } catch (DateTimeParseException e) {
            System.out.println("Failed to parse date '" + dateInput + "': " + e.getMessage() +
                    ", defaulting to 30 days from today");
            LocalDate defaultDate = today.plusDays(30);
            LocalDateTime endOfDay = defaultDate.atTime(23, 59, 59);
            return Timestamp.valueOf(endOfDay);
        }
    }

    @Override
    public String getDescription() {
        return "Creates a goal for a user with a simple due date. " +
                "Uses the current user context automatically. " +
                "Requires goalTitle, goalDescription (optional), and dueTime.";
    }


     @Override
    public ToolParameters getParameters() {
        return ToolParameters.builder()
                .type("object")
                .properties(Map.of(
                        "goalTitle", Map.of("type", "string", "description", "The title of the goal"),
                        "goalDescription", Map.of("type", "string", "description", "The description of the goal (optional)"),
                        "dueTime", Map.of("type", "string", "description",
                                "The due date for the goal. Can be ISO date (yyyy-MM-dd) or other formats (MM/dd/yyyy, MM-dd-yyyy). " +
                                "Goals cannot have due dates in the past."),
                        "tags", Map.of("type", "array", "items", Map.of("type", "string"), "description", "Optional array of tags for the goal")))
                .required(Arrays.asList("goalTitle", "dueTime"))
                .build();
    }
}