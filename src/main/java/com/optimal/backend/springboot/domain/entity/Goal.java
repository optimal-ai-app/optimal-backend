package com.optimal.backend.springboot.domain.entity;

import jakarta.persistence.*;
import java.util.UUID;
import java.sql.Timestamp;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

@Entity
@Table(name = "goals")
public class Goal {

    @Id
    private UUID id;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "goal_title")
    private String goalTitle;

    @Column(name = "goal_description")
    private String goalDescription;

    @Column(name = "due_date")
    private Timestamp dueDate;

    @Column(name = "created_at")
    private Timestamp createdAt;
}
