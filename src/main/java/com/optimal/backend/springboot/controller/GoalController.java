// src/main/java/com/optimal/backend/springboot/controller/GoalController.java
package com.optimal.backend.springboot.controller;

import com.optimal.backend.springboot.domain.entity.Goal;
import com.optimal.backend.springboot.service.GoalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/goals")
public class GoalController {

    private final GoalService goalService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Goal>> getGoalsByUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(goalService.getGoalsByUser(userId));
    }
}
