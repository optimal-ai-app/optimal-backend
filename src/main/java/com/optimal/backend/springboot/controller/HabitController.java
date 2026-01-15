// src/main/java/com/optimal/backend/springboot/controller/HabitController.java
package com.optimal.backend.springboot.controller;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.optimal.backend.springboot.database.entity.Habit;
import com.optimal.backend.springboot.database.entity.HabitAction;
import com.optimal.backend.springboot.database.entity.HabitLog;
import com.optimal.backend.springboot.security.annotation.CurrentUser;
import com.optimal.backend.springboot.security.model.TokenUserContext;
import com.optimal.backend.springboot.service.HabitService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/habits")
public class HabitController {

	private final HabitService habitService;

	// Habits CRUD
	@PostMapping
	public ResponseEntity<Habit> createHabit(@RequestBody Habit habit, @CurrentUser TokenUserContext userContext) {
		habit.setUserId(userContext.getUserId());
		return ResponseEntity.ok(habitService.createHabit(habit));
	}

	@GetMapping
	public ResponseEntity<List<Habit>> getAllHabits() {
		return ResponseEntity.ok(habitService.getAllHabits());
	}

	@GetMapping("/{habitId}")
	public ResponseEntity<Habit> getHabitById(@PathVariable UUID habitId) {
		Optional<Habit> habit = habitService.getHabitById(habitId);
		return habit.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
	}

	@PutMapping("/{habitId}")
	public ResponseEntity<Habit> updateHabit(@PathVariable UUID habitId, @RequestBody Habit habit) {
		if (!habitService.habitExists(habitId)) {
			return ResponseEntity.notFound().build();
		}
		habit.setId(habitId);
		return ResponseEntity.ok(habitService.updateHabit(habit));
	}

	@DeleteMapping("/{habitId}")
	public ResponseEntity<Void> deleteHabit(@PathVariable UUID habitId) {
		if (!habitService.habitExists(habitId)) {
			return ResponseEntity.notFound().build();
		}
		habitService.deleteHabit(habitId);
		return ResponseEntity.noContent().build();
	}

	// Nested: Actions
	@PostMapping("/{habitId}/actions")
	public ResponseEntity<HabitAction> addHabitAction(@PathVariable UUID habitId, @RequestBody HabitAction action) {
		action.setHabitId(habitId);
		return ResponseEntity.ok(habitService.addHabitAction(action));
	}

	@GetMapping("/{habitId}/actions")
	public ResponseEntity<List<HabitAction>> listHabitActions(@PathVariable UUID habitId) {
		return ResponseEntity.ok(habitService.listHabitActions(habitId));
	}

	@DeleteMapping("/actions/{actionId}")
	public ResponseEntity<Void> deleteHabitAction(@PathVariable UUID actionId) {
		habitService.deleteHabitAction(actionId);
		return ResponseEntity.noContent().build();
	}

	// Nested: Logs
	@PostMapping("/{habitId}/logs")
	public ResponseEntity<HabitLog> addHabitLog(@PathVariable UUID habitId, @RequestBody HabitLog log) {
		log.setHabitId(habitId);
		return ResponseEntity.ok(habitService.addHabitLog(log));
	}

	@GetMapping("/{habitId}/logs")
	public ResponseEntity<List<HabitLog>> listHabitLogs(@PathVariable UUID habitId) {
		return ResponseEntity.ok(habitService.listHabitLogs(habitId));
	}

	@DeleteMapping("/logs/{logId}")
	public ResponseEntity<Void> deleteHabitLog(@PathVariable UUID logId) {
		habitService.deleteHabitLog(logId);
		return ResponseEntity.noContent().build();
	}
}


