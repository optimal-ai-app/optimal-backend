package com.optimal.backend.springboot.controller.RequestClasses;

import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateDiaryLogRequest {
    private UUID userId;
    private String transcript;
} 