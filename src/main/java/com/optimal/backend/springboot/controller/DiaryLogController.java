package com.optimal.backend.springboot.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.optimal.backend.springboot.controller.RequestClasses.CreateDiaryLogRequest;
import com.optimal.backend.springboot.database.entity.DiaryLog;
import com.optimal.backend.springboot.service.DiaryLogService;

@RestController
@RequestMapping("/api/diary")
public class DiaryLogController {
    @Autowired
    private DiaryLogService diaryLogService;

    @PostMapping("/create")
    public DiaryLog createLog(@RequestBody CreateDiaryLogRequest request) {

        return diaryLogService.createDiaryLog(request.getUserId(), request.getTranscript());

    }

}
