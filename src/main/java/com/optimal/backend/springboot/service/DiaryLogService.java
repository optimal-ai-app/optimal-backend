package com.optimal.backend.springboot.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.optimal.backend.springboot.agent.framework.agents.DiaryLogAgent;
import com.optimal.backend.springboot.agent.framework.core.Message;
import com.optimal.backend.springboot.agent.framework.core.UserContext;
import com.optimal.backend.springboot.database.entity.DiaryLog;
import com.optimal.backend.springboot.database.entity.Tag;
import com.optimal.backend.springboot.database.repository.DiaryLogRepository;
import com.optimal.backend.springboot.database.repository.TagRepository;
import com.optimal.backend.springboot.utils.DateUtils;
import com.optimal.backend.springboot.utils.DateUtils.ToFromDate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

@Service
@Transactional
public class DiaryLogService {
    @Autowired
    private DiaryLogRepository diaryLogRepo;
    @Autowired
    private DiaryLogAgent diaryLogAgent;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private TagRepository tagRepository;
    @Autowired
    private TaskService taskService;

    public DiaryLog createDiaryLog(UUID userId, String transcript) {
        long startTime = System.nanoTime();
        // Create DiaryLog entity
        DiaryLog diaryLog = new DiaryLog();
        diaryLog.setUserId(userId);
        diaryLog.setTranscript(transcript);
        UserContext.setUserId(userId);

        // Process transcript with DiaryLogAgent
        List<Message> instructions = new ArrayList<>();
        instructions.add(new Message("user", "Date: " + diaryLog.getDate() + "\n" + transcript));

        List<Message> response = diaryLogAgent.run(instructions);

        // Extract summary from agent response
        String agentResponse = response.get(response.size() - 1).getContent();
        DiaryJsonResponse diaryJsonResponse = extractDiaryJsonResponse(agentResponse);
        diaryLog.setSummary(diaryJsonResponse.summary);

        try {
            DiaryLog savedDiaryLog = diaryLogRepo.save(diaryLog);

            // update tags
            tagRepository.saveAll(diaryJsonResponse.tags.stream().map(tag -> new Tag(tag, savedDiaryLog.getId()))
                    .collect(Collectors.toList()));
            taskService.updateTasks(diaryJsonResponse.tasks);
            diaryLog = savedDiaryLog;
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new RuntimeException("Failed to save diary log due to concurrent modification. Please try again.", e);
        }

        // update tasks and goals
        return diaryLog;
    }

    private DiaryJsonResponse extractDiaryJsonResponse(String agentResponse) {
        try {
            DiaryJsonResponse response = new DiaryJsonResponse();
            JsonNode jsonNode = objectMapper.readTree(agentResponse);
            if (jsonNode.has("content")) {
                response.summary = jsonNode.get("content").get("summary").asText();
            }
            JsonNode tasksNode = jsonNode.get("content").get("tasksToUpdate");
            if (tasksNode.isArray()) {
                response.tasks = new ArrayList<>();
                for (JsonNode task : tasksNode) {
                    response.tasks.add(task.asText());
                }
            }
            JsonNode tagsNode = jsonNode.get("tags");
            if (tagsNode.isArray()) {
                response.tags = new ArrayList<>();
                for (JsonNode tag : tagsNode) {
                    response.tags.add(tag.asText());
                }
            }

            return response;
        } catch (Exception e) {
            return null;
        }
    }

    public String getDiaryLogsForWeek(UUID userId) {
        ToFromDate toFromDate = DateUtils.getDates();
        List<DiaryLog> diaryLogs = diaryLogRepo.findByUserAndDateBetween(userId, toFromDate.startDate,
                toFromDate.endDate);
        StringBuilder diaryLogsString = new StringBuilder();
        for (DiaryLog diaryLog : diaryLogs) {
            diaryLogsString.append(diaryLog.getSummary());
        }

        return diaryLogsString.toString();
    }

    public String getTagsForWeek(UUID userId) {
        ToFromDate toFromDate = DateUtils.getDates();
        List<Tag> tags = tagRepository.findByUserAndDateBetween(userId, toFromDate.startDate, toFromDate.endDate);
        StringBuilder tagsString = new StringBuilder();
        for (Tag tag : tags) {
            tagsString.append(tag.getName());
        }
        return tagsString.toString();
    }

    private class DiaryJsonResponse {
        public String summary;
        public List<String> tasks;
        public List<String> tags;
    }

}
