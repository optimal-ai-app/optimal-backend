// src/main/java/com/optimal/backend/springboot/controller/GoalController.java
package com.optimal.backend.springboot.controller;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.optimal.backend.springboot.controller.RequestClasses.CreateGoalRequest;
import com.optimal.backend.springboot.database.entity.Goal;
import com.optimal.backend.springboot.service.GoalService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/goals")
public class GoalController {

    private final GoalService goalService;

    @PostMapping("/create")
    public ResponseEntity<Goal> createGoal(@RequestBody CreateGoalRequest request) {
        try {
            System.out.println("=== /api/goals/create received ===");
            System.out.println("request.userId = " + request.getUserId());
            System.out.println("title           = " + request.getTitle());
            Goal createdGoal = goalService.createGoal(request);
            return ResponseEntity.ok(createdGoal);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Goal>> getGoalsByUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(goalService.getGoalsByUser(userId));
    }

    @GetMapping("/{goalId}")
    public ResponseEntity<Goal> getGoalById(@PathVariable UUID goalId) {
        Optional<Goal> goal = goalService.getGoalById(goalId);
        return goal.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{goalId}")
    public ResponseEntity<Goal> updateGoal(@PathVariable UUID goalId, @RequestBody Goal goal) {
        try {
            if (!goalService.existsById(goalId)) {
                return ResponseEntity.notFound().build();
            }
            Goal updatedGoal = goalService.updateGoal(goal);
            return ResponseEntity.ok(updatedGoal);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build(); // 409 Conflict for optimistic locking
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{goalId}")
    public ResponseEntity<Void> deleteGoal(@PathVariable UUID goalId) {
        try {
            if (!goalService.existsById(goalId)) {
                return ResponseEntity.notFound().build();
            }
            goalService.deleteGoal(goalId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build(); // 409 Conflict for optimistic locking
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
