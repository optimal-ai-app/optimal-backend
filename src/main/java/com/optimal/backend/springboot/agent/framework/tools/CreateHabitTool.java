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
	public String execute(@P("habitTitle") String habitTitle, @P("description") String description, @P("cadence") String cadence, @P("tags") String tags, @P("actions") String[] actions) {
		try {
			UUID userId = UserContext.requireUserId();
			Habit habit = new Habit();
			habit.setUserId(userId);
			habit.setTitle(habitTitle);
			habit.setDescription(description);
			habit.setCadence(cadence);
			habit.setStreak(0);
			habit.setHealth(50);
			habit.setIsActive(true);
			habit.setTags(tags);
			Habit saved = habitService.createHabit(habit);

			// Seed actions from provided list (LLM produced) if any
			List<HabitAction> createdActions = new ArrayList<>();
			if (actions != null && actions.length > 0) {
				for (String actionTitle : actions) {
					HabitAction action = new HabitAction();
					action.setHabitId(saved.getId());
					action.setTitle(actionTitle);
					action.setRecurrenceRule(cadence);
					createdActions.add(habitService.addHabitAction(action));
				}
			}

			return "Habit '" + habitTitle + "' created with " + createdActions.size() + " actions.";
		} catch (Exception e) {
			return "Error creating habit: " + e.getMessage();
		}
	}


}


