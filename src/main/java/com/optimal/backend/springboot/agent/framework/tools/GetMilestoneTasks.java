package com.optimal.backend.springboot.agent.framework.tools;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.optimal.backend.springboot.agent.framework.core.Tool;
import com.optimal.backend.springboot.agent.framework.core.UserContext;
import com.optimal.backend.springboot.database.entity.Task;
import com.optimal.backend.springboot.service.TaskService;

@Component
public class GetMilestoneTasks implements Tool {

    @Autowired
    private TaskService taskService;

    @Override
    public String getName() {
        return "get_tasks_before_due_date";
    }

    @Override
    public String getDescription() {
        return "Given a milestone taskId, returns non-milestone tasks for the same user with due dates before that task's due date, sorted by completion status then due date. Includes the input task's due date in the response.";
    }

    @Override
    public String execute(String input) {
        try {
            JsonNode node = new ObjectMapper().readTree(input);
            UUID taskId = UUID.fromString(node.get("taskId").asText());

            Task anchorTask = taskService.getTaskById(taskId)
                    .orElseThrow(() -> new IllegalArgumentException("Task not found"));

            if (anchorTask.getMilestone() == null || !anchorTask.getMilestone()) {
                return "The provided task is not a milestone task.";
            }

            UUID userId = anchorTask.getUserId() != null ? anchorTask.getUserId() : UserContext.requireUserId();
            Timestamp dueDate = anchorTask.getDueDate();

            if (dueDate == null) {
                return "The referenced task has no due date; cannot fetch tasks before due date.";
            }

            List<Task> tasks = taskService.getUserTasksBeforeDueDate(userId, dueDate);

            StringBuilder sb = new StringBuilder();
            sb.append("Non-milestone tasks due before ").append(dueDate).append(" for milestone task ").append(taskId)
                    .append(":\n\n");
            if (tasks == null || tasks.isEmpty()) {
                sb.append("No tasks found before this due date.");
                return sb.toString();
            }

            for (Task t : tasks) {
                sb.append("- [").append("completed".equalsIgnoreCase(t.getStatus()) ? "x" : " ")
                        .append("] ")
                        .append(t.getTitle())
                        .append(" (due ").append(t.getDueDate()).append(")")
                        .append(" — status: ").append(t.getStatus())
                        .append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}
