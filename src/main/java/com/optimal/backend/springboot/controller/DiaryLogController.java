package com.optimal.backend.springboot.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.optimal.backend.springboot.agent.framework.agents.DiaryLogAgent;
import com.optimal.backend.springboot.agent.framework.core.Message;
import com.optimal.backend.springboot.controller.RequestClasses.CreateDiaryLogRequest;
import com.optimal.backend.springboot.domain.entity.DiaryLog;
import com.optimal.backend.springboot.domain.repository.DiaryLogRepository;
import com.optimal.backend.springboot.domain.repository.TagRepository;
import com.optimal.backend.springboot.agent.framework.core.UserContext;

@RestController
@RequestMapping("/api/diary")
public class DiaryLogController {
    private final DiaryLogRepository diaryLogRepo;
    private final TagRepository tagRepo;
    private final DiaryLogAgent diaryLogAgent;
    private final ObjectMapper objectMapper;

    public DiaryLogController(DiaryLogRepository diaryLogRepo,
            TagRepository tagRepo,
            DiaryLogAgent diaryLogAgent) {
        this.diaryLogRepo = diaryLogRepo;
        this.tagRepo = tagRepo;
        this.diaryLogAgent = diaryLogAgent;
        this.objectMapper = new ObjectMapper();
    }

    @PostMapping("/create")
    public DiaryLog createLog(@RequestBody CreateDiaryLogRequest request) {
        try {
            // Create DiaryLog entity
            DiaryLog diaryLog = new DiaryLog();
            diaryLog.setUserId(request.getUserId());
            diaryLog.setTranscript(request.getTranscript());
            UserContext.setUserId(request.getUserId());

            // Process transcript with DiaryLogAgent
            List<Message> instructions = new ArrayList<>();
            instructions.add(new Message("user", "Date: " + diaryLog.getDate() + "\n" + request.getTranscript()));

            List<Message> response = diaryLogAgent.run(instructions);

            // Extract summary from agent response
            if (!response.isEmpty()) {
                String agentResponse = response.get(response.size() - 1).getContent();
                String summary = extractSummaryFromResponse(agentResponse);
                diaryLog.setSummary(summary);
            }

            // Save the diary log
            return diaryLogRepo.save(diaryLog);
        } catch (Exception e) {
            // Log the error and save without summary if agent processing fails
            System.err.println("Error processing diary log with agent: " + e.getMessage());

            DiaryLog diaryLog = new DiaryLog();
            diaryLog.setUserId(request.getUserId());
            diaryLog.setTranscript(request.getTranscript());
            diaryLog.setSummary(null); // No summary if processing fails
            return diaryLogRepo.save(diaryLog);

        } finally {
            UserContext.clear();
        }
    }

    private String extractSummaryFromResponse(String agentResponse) {
        try {
            JsonNode jsonNode = objectMapper.readTree(agentResponse);

            // Check if response has the new format with "content"
            if (jsonNode.has("content") && jsonNode.get("content").has("summary")) {
                return jsonNode.get("content").get("summary").asText();
            }
            // Fallback to old format
            else if (jsonNode.has("summary")) {
                return jsonNode.get("summary").asText();
            }

            return "Summary extraction failed - invalid response format";

        } catch (Exception e) {
            System.err.println("Error parsing agent response: " + e.getMessage());
            return "Summary extraction failed - parsing error";
        }
    }
}
