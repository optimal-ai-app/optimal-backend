package com.optimal.backend.springboot.service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.optimal.backend.springboot.database.entity.GoalProgress;
import com.optimal.backend.springboot.database.repository.GoalProgressPrepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GoalProgressService {

    @Autowired
    private GoalProgressPrepository goalProgressPrepository;
    
    public List<GoalProgress> getGoalProgressByGoalId(UUID goalId) {
        return goalProgressPrepository.findByGoalId(goalId);
    }
}
