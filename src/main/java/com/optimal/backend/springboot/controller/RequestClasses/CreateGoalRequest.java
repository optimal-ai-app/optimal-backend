package com.optimal.backend.springboot.controller.RequestClasses;

import java.sql.Timestamp;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateGoalRequest {
    private String title;
    private String description;
    private Timestamp dueDate;
    private String[] tags;
}
