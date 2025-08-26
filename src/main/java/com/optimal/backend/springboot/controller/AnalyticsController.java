package com.optimal.backend.springboot.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.optimal.backend.springboot.controller.RequestClasses.GetWeeklyLogRequest;
import com.optimal.backend.springboot.service.WeeklyLogService;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {
    @Autowired
    private WeeklyLogService WeeklyLogService;

    @PostMapping("/get-weekly-log")
    public GetWeeklyLogRequest getWeeklyLog(@RequestBody GetWeeklyLogRequest request) {
        return WeeklyLogService.getWeeklyLog(request.getUserId());
    }
}
