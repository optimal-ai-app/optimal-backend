package com.optimal.backend.springboot.controller.RequestClasses;

import java.sql.Timestamp;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateGoalRequest {
    private UUID userId;
    private String title;
    private String description;
    private Timestamp dueDate;
    private String[] tags;
}
