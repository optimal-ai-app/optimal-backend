package com.optimal.backend.springboot.controller.RequestClasses;

import lombok.Getter;
import lombok.Setter;
import java.util.UUID;

@Getter
@Setter
public class GetWeeklyLogRequest {
    private UUID userId;
    private String log;
}
