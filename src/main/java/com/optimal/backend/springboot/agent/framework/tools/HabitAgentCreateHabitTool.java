package com.optimal.backend.springboot.agent.framework.tools;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.optimal.backend.springboot.agent.framework.core.Tool;
import com.optimal.backend.springboot.agent.framework.core.UserContext;
import com.optimal.backend.springboot.database.entity.Habit;
import com.optimal.backend.springboot.database.entity.HabitAction;
import com.optimal.backend.springboot.database.entity.HabitLog;
import com.optimal.backend.springboot.service.HabitService;

import dev.langchain4j.agent.tool.ToolParameters;

@Component
public class HabitAgentCreateHabitTool implements Tool {

	@Autowired
	private HabitService habitService;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public String getName() {
		return "createHabit";
	}

	@Override
	public String execute(String input) {
		try {
			UUID userId = UserContext.requireUserId();
			JsonNode node = objectMapper.readTree(input);

			String title = text(node, "habitTitle");
			String type = text(node, "type");
			String cadenceRule = text(node, "cadenceRule");
			String adherencePolicy = text(node, "adherencePolicy");
			String verificationMethod = text(node, "verificationMethod");
			String notifyMode = text(node, "notifyMode");

			Habit habit = new Habit();
			habit.setType(type);
			habit.setCadenceRule(cadenceRule);
			habit.setAdherencePolicy(adherencePolicy);
			habit.setVerificationMethod(verificationMethod);
			habit.setNotifyMode(notifyMode);
			habit.setHealthScore(50);
			Habit saved = habitService.createHabit(habit);

			// Seed actions from provided list (LLM produced) if any
			List<HabitAction> createdActions = new ArrayList<>();
			if (node.has("actions") && node.get("actions").isArray()) {
				for (JsonNode a : node.get("actions")) {
					String actionTitle = a.has("title") ? a.get("title").asText() : title;
					HabitAction action = new HabitAction();
					action.setHabitId(saved.getId());
					action.setTitle(actionTitle);
					action.setRecurrenceRule(cadenceRule);
					createdActions.add(habitService.addHabitAction(action));
				}
			}

			return "Habit '" + title + "' created with " + createdActions.size() + " actions.";
		} catch (Exception e) {
			return "Error creating habit: " + e.getMessage();
		}
	}

	@Override
	public String getDescription() {
		return "Creates a habit, seeds recurring habit_actions from cadence, and prepares for quick logging.";
	}

	@Override
	public ToolParameters getParameters() {
		return ToolParameters.builder()
				.type("object")
				.properties(Map.of(
						"habitTitle", Map.of("type", "string"),
						"type", Map.of("type", "string", "enum", List.of("Positive Action", "Abstinence", "Controlled Use")),
						"cadenceRule", Map.of("type", "string", "description", "RRULE string e.g., FREQ=WEEKLY;BYDAY=MO,WE,FR"),
						"adherencePolicy", Map.of("type", "string"),
						"verificationMethod", Map.of("type", "string", "enum", List.of("self-check", "os_screen_time", "wearable")),
						"notifyMode", Map.of("type", "string", "enum", List.of("light", "standard", "intensive")),
						"actions", Map.of("type", "array", "items", Map.of("type", "object", "properties", Map.of("title", Map.of("type", "string"))))))
				.required(List.of("habitTitle", "type", "cadenceRule", "notifyMode"))
				.build();
	}

	private String text(JsonNode node, String field) {
		return node.has(field) && !node.get(field).isNull() ? node.get(field).asText() : null;
	}
}


