package com.optimal.backend.springboot.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.optimal.backend.springboot.database.entity.Habit;
import com.optimal.backend.springboot.database.entity.HabitAction;
import com.optimal.backend.springboot.database.entity.HabitLog;
import com.optimal.backend.springboot.database.repository.HabitActionRepository;
import com.optimal.backend.springboot.database.repository.HabitLogRepository;
import com.optimal.backend.springboot.database.repository.HabitRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HabitService {

	private final HabitRepository habitRepository;
	private final HabitActionRepository habitActionRepository;
	private final HabitLogRepository habitLogRepository;

	// Habits
	public Habit createHabit(Habit habit) {
		return habitRepository.save(habit);
	}

	public Optional<Habit> getHabitById(UUID id) {
		return habitRepository.findById(id);
	}

	public Habit requireHabit(UUID id) {
		return habitRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Habit not found"));
	}

	public List<Habit> getAllHabits() {
		return habitRepository.findAll();
	}

	public Habit updateHabit(Habit habit) {
		return habitRepository.save(habit);
	}

	public void deleteHabit(UUID id) {
		habitRepository.deleteById(id);
	}

	public boolean habitExists(UUID id) {
		return habitRepository.existsById(id);
	}

	// Actions
	public HabitAction addHabitAction(HabitAction action) {
		if (action.getHabitId() == null || !habitRepository.existsById(action.getHabitId())) {
			throw new IllegalArgumentException("Habit not found for action");
		}
		return habitActionRepository.save(action);
	}

	public List<HabitAction> listHabitActions(UUID habitId) {
		return habitActionRepository.findByHabitId(habitId);
	}

	@Transactional
	public void deleteHabitAction(UUID actionId) {
		habitActionRepository.deleteById(actionId);
	}

	// Logs
	public HabitLog addHabitLog(HabitLog log) {
		if (log.getHabitId() == null || !habitRepository.existsById(log.getHabitId())) {
			throw new IllegalArgumentException("Habit not found for log");
		}
		return habitLogRepository.save(log);
	}

	public List<HabitLog> listHabitLogs(UUID habitId) {
		return habitLogRepository.findByHabitId(habitId);
	}

	@Transactional
	public void deleteHabitLog(UUID logId) {
		habitLogRepository.deleteById(logId);
	}
}


