package com.optimal.backend.springboot.database.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.optimal.backend.springboot.database.entity.GoalProgress;

public interface GoalProgressRepository extends JpaRepository<GoalProgress, UUID> {
    @Query("SELECT g FROM GoalProgress g WHERE g.goalId = :goalId")
    List<GoalProgress> findByGoalId(@Param("goalId") UUID goalId);

    @Query("SELECT g FROM GoalProgress g WHERE g.goalId IN :goalIds")
    List<GoalProgress> findByGoalIdIn(@Param("goalIds") List<UUID> goalIds);
}
