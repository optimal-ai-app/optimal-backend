package com.optimal.backend.springboot.service;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.sql.Date;
import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.optimal.backend.springboot.controller.RequestClasses.GetWeeklyLogRequest;
import com.optimal.backend.springboot.database.entity.WeeklyLog;
import com.optimal.backend.springboot.agent.framework.agents.WeeklyLogAgent;
import com.optimal.backend.springboot.agent.framework.core.Message;
import com.optimal.backend.springboot.utils.DateUtils;
import com.optimal.backend.springboot.utils.DateUtils.ToFromDate;
import java.util.UUID;
import com.optimal.backend.springboot.database.repository.WeeklyLogRespository;
@Service
public class WeeklyLogService {

    @Autowired
    private DiaryLogService diaryLogService;
    @Autowired
    private WeeklyLogAgent weeklyLogAgent;
    @Autowired
    private WeeklyLogRespository weeklyLogRepository;
    public GetWeeklyLogRequest getWeeklyLog(UUID userId) {
        // Check if a weekly log exists within the last 7 days
        Date sevenDaysAgo = Date.valueOf(LocalDate.now().minusDays(7));
        Optional<WeeklyLog> existingLog = weeklyLogRepository.findByUserIdWithinSevenDays(userId, sevenDaysAgo);
        
        if (existingLog.isPresent()) {
            // Return existing log if found within 7 days
            GetWeeklyLogRequest weeklyLogResponse = new GetWeeklyLogRequest();
            weeklyLogResponse.setLog(existingLog.get().getLog());
            return weeklyLogResponse;
        }
        
        // Generate new weekly log if none exists within 7 days
        String diaryLogs = diaryLogService.getDiaryLogsForWeek(userId);
        String tags = diaryLogService.getTagsForWeek(userId);
        ToFromDate toFromDate = DateUtils.getDates();
        String logs = "Weekly Log from " + toFromDate.startDate + " to " + toFromDate.endDate +
                "\nDiary Logs:\n" + diaryLogs + "\nTags:\n" + tags;
        List<Message> instructions = new ArrayList<>();
        instructions.add(new Message("user", logs));
        List<Message> response = weeklyLogAgent.run(instructions);
        String agentResponse = response.get(response.size() - 1).getContent();

        GetWeeklyLogRequest weeklyLogResponse = new GetWeeklyLogRequest();
        weeklyLogResponse.setLog(agentResponse);
        System.out.println(agentResponse);
        WeeklyLog weeklyLog = new WeeklyLog();
        weeklyLog.setLog(agentResponse);
        weeklyLog.setStartDate(toFromDate.startDate);
        weeklyLog.setEndDate(toFromDate.endDate);
        weeklyLog.setUserId(userId);
        weeklyLogRepository.save(weeklyLog);
        return weeklyLogResponse;
    }

}
