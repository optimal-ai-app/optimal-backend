package com.optimal.backend.springboot.controller.RequestClasses;

import java.util.UUID;

import lombok.Data;

@Data
public class UpdateTaskRequest {
    private UUID taskId;
    private String status;
    private String completionDate;
    private String updatedAt;

    public UUID getTaskId() {
        return taskId;
    }

    public String toString() {
        return "UpdateTaskRequest{" +
                "taskId=" + taskId +
                ", status='" + status + '\'' +
                ", completionDate='" + completionDate + '\'' +
                ", updatedAt='" + updatedAt + '\'' +
                '}';
    }
}
