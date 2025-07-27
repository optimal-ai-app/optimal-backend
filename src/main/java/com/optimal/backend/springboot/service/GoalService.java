// src/main/java/com/optimal/backend/springboot/service/GoalService.java
package com.optimal.backend.springboot.service;

import com.optimal.backend.springboot.controller.RequestClasses.CreateGoalRequest;
import com.optimal.backend.springboot.domain.entity.Goal;
import com.optimal.backend.springboot.domain.repository.GoalRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class GoalService {

    @Autowired
    private final GoalRepository goalRepository;

    @Transactional(readOnly = true)
    public List<Goal> getGoalsByUser(UUID userId) {
        return goalRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public Optional<Goal> getGoalById(UUID goalId) {
        return goalRepository.findById(goalId);
    }

    public Goal createGoal(CreateGoalRequest request) {
        System.out.println("=== GoalService.createGoal ===");
        System.out.println("userId in request = " + request.getUserId());

        Goal goal = new Goal();
        goal.setUserId(request.getUserId());
        goal.setTitle(request.getTitle());
        goal.setDescription(request.getDescription());
        goal.setDueDate(request.getDueDate());
        goal.setTags(request.getTags());
        goal.setStatus("active");
        goal.setProgress(0);
        goal.setStreak(0);
        return goalRepository.save(goal);
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
