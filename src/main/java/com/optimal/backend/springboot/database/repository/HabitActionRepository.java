package com.optimal.backend.springboot.database.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.optimal.backend.springboot.database.entity.HabitAction;

public interface HabitActionRepository extends JpaRepository<HabitAction, UUID> {
    java.util.List<HabitAction> findByHabitId(UUID habitId);
}


