// src/main/java/com/optimal/backend/springboot/service/GoalService.java
package com.optimal.backend.springboot.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.optimal.backend.springboot.controller.RequestClasses.CreateGoalRequest;
import com.optimal.backend.springboot.database.entity.Goal;
import com.optimal.backend.springboot.database.entity.GoalProgress;
import com.optimal.backend.springboot.database.entity.GoalType;
import com.optimal.backend.springboot.database.repository.GoalProgressRepository;
import com.optimal.backend.springboot.database.repository.GoalRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class GoalService {

    @Autowired
    private final GoalRepository goalRepository;

    @Autowired
    private GoalProgressRepository goalProgressRepository;

    @Transactional(readOnly = true)
    public List<Goal> getGoalsByUser(UUID userId) {
        return goalRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public Optional<Goal> getGoalById(UUID goalId) {
        return goalRepository.findById(goalId);
    }

    public Goal createGoal(CreateGoalRequest request, UUID userId) {
        System.out.println("=== GoalService.createGoal ===");

        Goal goal = new Goal();
        goal.setUserId(userId);
        goal.setTitle(request.getTitle());
        goal.setDescription(request.getDescription());
        goal.setDueDate(request.getDueDate());
        goal.setTags(request.getTags());
        goal.setStatus("active");
        goal.setStreak(0);

        Goal savedGoal = goalRepository.save(goal);

        GoalProgress goalProgress = new GoalProgress();
        goalProgress.setGoalId(savedGoal.getId());
        goalProgress.setGoalType(GoalType.QUALITATIVE);
        goalProgress.setTotalUnits(0.0);
        goalProgress.setCompletedUnits(0.0);
        goalProgress.setScore(0.0);
        goalProgressRepository.save(goalProgress);

        return savedGoal;
    }

    public Goal updateGoal(Goal goal) {
        try {
            return goalRepository.save(goal);
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new RuntimeException("Goal was modified by another user. Please refresh and try again.", e);
        }
    }

    public void deleteGoal(UUID goalId) {
        try {
            goalRepository.deleteById(goalId);
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new RuntimeException("Goal was modified by another user. Please refresh and try again.", e);
        }
    }

    public boolean existsById(UUID goalId) {
        return goalRepository.existsById(goalId);
    }

    @Transactional(readOnly = true)
    public Optional<Goal> getGoalByUserIdAndTitle(UUID userId, String title) {
        List<Goal> goals = goalRepository.findByUserIdAndTitle(userId, title);
        if (goals != null && !goals.isEmpty()) {
            return Optional.of(goals.get(0));
        }
        return Optional.empty();
    }
}
