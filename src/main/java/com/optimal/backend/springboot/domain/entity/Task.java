// src/main/java/com/optimal/backend/springboot/domain/entity/Task.java
package com.optimal.backend.springboot.domain.entity;

import jakarta.persistence.*;
import java.util.UUID;

import com.optimal.backend.springboot.model.User;

import java.sql.Timestamp;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "tasks")
public class Task {

    @Id
    @Column(name = "task_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID taskId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "created_date", nullable = false)
    private Timestamp createdDate;

    @Column(name="completed_date")
    private Timestamp completedDate;

    @Column(name = "priority", nullable = false)
    private String priority;

    @Column(name = "due_date")
    private Timestamp dueDate;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "goal_id")
    private UUID goalId;

    @Column(name = "user_id")
    private UUID userId;


    @PrePersist
    protected void onCreate() {
        this.createdDate = new Timestamp(System.currentTimeMillis());
    }
}
