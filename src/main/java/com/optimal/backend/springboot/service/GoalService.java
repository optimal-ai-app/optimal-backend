// src/main/java/com/optimal/backend/springboot/service/GoalService.java
package com.optimal.backend.springboot.service;

import com.optimal.backend.springboot.domain.entity.Goal;
import com.optimal.backend.springboot.domain.repository.GoalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GoalService {

    private final GoalRepository goalRepository;

    public List<Goal> getGoalsByUser(UUID userId) {
        return goalRepository.findByUserId(userId);
    }
}
