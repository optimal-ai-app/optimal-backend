// src/main/java/com/optimal/backend/springboot/service/GoalService.java
package com.optimal.backend.springboot.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.optimal.backend.springboot.agent.framework.agents.GoalClassifierAgent;
import com.optimal.backend.springboot.agent.framework.core.Message;
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
    private GoalClassifierAgent goalClassifierAgent;

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
        goal.setStreak(0);

        Goal savedGoal = goalRepository.save(goal);
        List<Message> instructions = new ArrayList<>();
        instructions.add(new Message("user",
                "\nDescription: " + savedGoal.getDescription()));
        List<Message> response = goalClassifierAgent.run(instructions);
        String agentResponse = response.get(response.size() - 1).getContent();
        try {

            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(agentResponse).get("content");
            String goalType = jsonNode.get("type").asText().toLowerCase();

            double metric = 0;
            GoalType goalTypeEnum;
            if (goalType == "quantitative") {
                goalTypeEnum = GoalType.QUANTITATIVE;
                metric = Integer.parseInt(jsonNode.get("metric").asText());
            } else {
                goalTypeEnum = GoalType.QUALITATIVE;
            }

            GoalProgress goalProgress = new GoalProgress();
            goalProgress.setGoalId(savedGoal.getId());
            goalProgress.setGoalType(goalTypeEnum);
            goalProgress.setTotalUnits(metric);
            goalProgress.setCompletedUnits(0.0);
            goalProgress.setScore(0.0);
            goalProgressRepository.save(goalProgress);

        } catch (Exception e) {

            GoalProgress goalProgress = new GoalProgress();
            goalProgress.setGoalId(savedGoal.getId());
            goalProgress.setGoalType(GoalType.QUANTITATIVE);
            goalProgress.setTotalUnits(0.0);
            goalProgress.setCompletedUnits(0.0);
            goalProgress.setScore(0.0);
            goalProgressRepository.save(goalProgress);

        }

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
