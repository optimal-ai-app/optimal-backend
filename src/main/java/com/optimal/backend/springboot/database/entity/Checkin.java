package com.optimal.backend.springboot.domain.entity;

import jakarta.persistence.*;
import java.util.UUID;
import java.sql.Timestamp;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "checkins")
public class Checkin {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "todo_id")
    private UUID todoId;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "status")
    private String status;

    @Column(name = "note")
    private String note;

    @Column(name = "timestamp")
    private Timestamp timestamp;
}
