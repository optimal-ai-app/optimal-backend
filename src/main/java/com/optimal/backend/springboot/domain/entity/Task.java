// src/main/java/com/optimal/backend/springboot/domain/entity/Task.java
package com.optimal.backend.springboot.domain.entity;

import jakarta.persistence.*;
import java.util.UUID;
import java.sql.Timestamp;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

@Entity
@Table(name = "tasks")
public class Task {

    @Id
    @Column(name = "task_id")
    private UUID taskId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "todo_list_id")
    private TodoList todoList;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goal_id")
    private Goal goal;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "created_at", nullable = false)
    private Timestamp createdAt;

    @Column(name = "due_date")
    private Timestamp dueDate;

    @PrePersist
    protected void onCreate() {
        this.createdAt = new Timestamp(System.currentTimeMillis());
    }
}
