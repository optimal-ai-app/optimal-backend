package com.optimal.backend.springboot.database.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.optimal.backend.springboot.database.entity.Habit;

public interface HabitRepository extends JpaRepository<Habit, UUID> {
}


