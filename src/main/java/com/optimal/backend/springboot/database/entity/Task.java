// src/main/java/com/optimal/backend/springboot/domain/entity/Task.java
package com.optimal.backend.springboot.database.entity;

import java.sql.Timestamp;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;

@Data
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

    @Column(name = "completed_date")
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

    @Column(name = "shared_id", nullable = true)
    private UUID sharedId;

    @Column(name = "updated_at", nullable = true, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Timestamp updatedAt;

    @Override
    public String toString() {
        return "Task{" +
                "taskId=" + taskId +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", createdDate=" + createdDate +
                ", completedDate=" + completedDate +
                ", priority='" + priority + '\'' +
                ", dueDate=" + dueDate +
                ", status='" + status + '\'' +
                ", goalId=" + goalId +
                ", userId=" + userId +
                ", sharedId=" + sharedId +
                '}';
    }

    @PrePersist
    protected void onCreate() {
        this.createdDate = new Timestamp(System.currentTimeMillis());
    }
}
