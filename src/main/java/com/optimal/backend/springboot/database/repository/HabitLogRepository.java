package com.optimal.backend.springboot.database.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.optimal.backend.springboot.database.entity.HabitLog;

public interface HabitLogRepository extends JpaRepository<HabitLog, UUID> {
    java.util.List<HabitLog> findByHabitId(UUID habitId);
}


