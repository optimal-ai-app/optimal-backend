package com.optimal.backend.springboot.agent.framework.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.optimal.backend.springboot.agent.framework.core.UserContext;
import com.optimal.backend.springboot.database.entity.Habit;
import com.optimal.backend.springboot.database.entity.HabitAction;
import com.optimal.backend.springboot.service.HabitService;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;


@Component
public class CreateHabitTool {

	@Autowired
	private HabitService habitService;

	@Tool("Creates a habit, seeds recurring habit_actions from cadence, and prepares for quick logging.")
	public String execute(@P("habitTitle") String habitTitle, @P("type") String type, @P("cadenceRule") String cadenceRule, @P("adherencePolicy") String adherencePolicy, @P("verificationMethod") String verificationMethod, @P("notifyMode") String notifyMode, @P("actions") String[] actions) {
		try {
			UUID userId = UserContext.requireUserId();
			Habit habit = new Habit();
			habit.setUserId(userId);
			habit.setType(type);
			habit.setCadenceRule(cadenceRule);
			habit.setAdherencePolicy(adherencePolicy);
			habit.setVerificationMethod(verificationMethod);
			habit.setNotifyMode(notifyMode);
			habit.setHealthScore(50);
			Habit saved = habitService.createHabit(habit);

			// Seed actions from provided list (LLM produced) if any
			List<HabitAction> createdActions = new ArrayList<>();
			if (actions != null && actions.length > 0) {
				for (String actionTitle : actions) {
					HabitAction action = new HabitAction();
					action.setHabitId(saved.getId());
					action.setTitle(actionTitle);
					action.setRecurrenceRule(cadenceRule);
					createdActions.add(habitService.addHabitAction(action));
				}
			}

			return "Habit '" + habitTitle + "' created with " + createdActions.size() + " actions.";
		} catch (Exception e) {
			return "Error creating habit: " + e.getMessage();
		}
	}


}


