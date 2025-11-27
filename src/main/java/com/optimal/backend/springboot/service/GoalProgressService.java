package com.optimal.backend.springboot.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.optimal.backend.springboot.database.entity.GoalProgress;
import com.optimal.backend.springboot.database.entity.GoalType;
import com.optimal.backend.springboot.database.entity.Task;
import com.optimal.backend.springboot.database.repository.GoalProgressRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GoalProgressService {

    @Autowired
    private GoalProgressRepository goalProgressRepository;

    public List<GoalProgress> getGoalProgressByGoalId(UUID goalId) {
        return goalProgressRepository.findByGoalId(goalId);
    }

    public Map<UUID, GoalProgress> getGoalProgressByGoalIds(List<UUID> goalIds) {
        List<GoalProgress> progressList = goalProgressRepository.findByGoalIdIn(goalIds);
        return progressList.stream()
                .collect(Collectors.toMap(GoalProgress::getGoalId, Function.identity(), (a, b) -> a));
    }

    public void addTaskToProgress(Task task) {
        GoalProgress goalProgress = goalProgressRepository.findByGoalId(task.getGoalId()).get(0);
        if (goalProgress.getGoalType().equals(GoalType.QUALITATIVE)) {
            goalProgress.setTotalUnits(goalProgress.getTotalUnits() + 1);
            goalProgressRepository.save(goalProgress);
        }
    }

    // public void updateGoalProgress(Task task) {
    //     GoalProgress goalProgress = goalProgressRepository.findByGoalId(task.getGoalId()).get(0);
    //     if (task.getStatus().equals("completed")) {
    //         if (task.getValue() != null) {
    //             goalProgress.setCompletedUnits(goalProgress.getCompletedUnits() + task.getValue());
    //         } else {
    //             goalProgress.setCompletedUnits(goalProgress.getCompletedUnits() + 1);
    //         }
    //     } else {
    //         if (task.getValue() != null) {
    //             goalProgress.setCompletedUnits(goalProgress.getCompletedUnits() - task.getValue());
    //         } else {
    //             goalProgress.setCompletedUnits(goalProgress.getCompletedUnits() - 1);
    //         }
    //     }
    //     goalProgressRepository.save(goalProgress);
    // }
}
