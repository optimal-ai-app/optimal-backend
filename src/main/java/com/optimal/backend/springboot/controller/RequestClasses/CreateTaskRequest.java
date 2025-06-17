package com.optimal.backend.springboot.controller.RequestClasses;

import lombok.Getter;
import lombok.Setter;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class CreateTaskRequest {
    private UUID userId;
    private String title;
    private String description;
    private Timestamp dueDate;
    private String priority;
    private UUID goalId;
    private Integer repeat;
    private List<String> repeatDays;
}
