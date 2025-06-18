package com.optimal.backend.springboot.controller;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;
import java.util.HashMap;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import com.optimal.backend.springboot.agent.framework.agents.TaskAgent;
import com.optimal.backend.springboot.agent.framework.core.BaseSupervisor;
import com.optimal.backend.springboot.agent.framework.core.LlmClient;
import com.optimal.backend.springboot.agent.framework.core.Message;

@RestController
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    private TaskAgent taskAgent;

    @Autowired
    private LlmClient llmClient;

    // Map to store user-specific supervisors
    private final Map<String, BaseSupervisor> userSupervisors = new ConcurrentHashMap<>();

    @PostMapping
    public ResponseEntity<Map<String, Object>> chat(@RequestBody Map<String, Object> request) {
        // Extract userId from request
        String date = (String) request.get("date");
        String userId = (String) request.get("userId");
        if (userId == null || userId.trim().isEmpty()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("content", "userId is required");
            errorResponse.put("tags", new ArrayList<>());
            errorResponse.put("readyToHandoff", false);
            return ResponseEntity.badRequest().body(errorResponse);
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> messages = (List<Map<String, Object>>) request.get("messages");

        // Create a new list with userId as system message at the front
        List<Message> convertedMessages = new ArrayList<>();

        // Add userId as system message at the front
        convertedMessages.add(new Message("system", "User ID: " + userId));
        convertedMessages.add(new Message("system", "Date: " + date));
        // Add the rest of the messages
        messages.stream()
                .map(msg -> new Message(
                        (String) msg.get("role"),
                        (String) msg.get("content")))
                .forEach(convertedMessages::add);

        // Get or create user-specific supervisor with manually injected dependencies
        BaseSupervisor userSupervisor = userSupervisors.computeIfAbsent(userId, id -> {
            BaseSupervisor newSupervisor = new BaseSupervisor(llmClient);
            newSupervisor.addAgent("taskAgent", taskAgent);
            return newSupervisor;
        });

        // Execute supervisor with handoff support
        BaseSupervisor.SupervisorResponse supervisorResponse = userSupervisor.executeWithHandoff(convertedMessages);

        // Convert to the expected response format
        Map<String, Object> response = new HashMap<>();
        response.put("content", supervisorResponse.content);
        response.put("tags", supervisorResponse.tags);
        response.put("readyToHandoff", supervisorResponse.readyToHandoff);

        System.out.println("output: " + response);
        return ResponseEntity.ok(response);
    }
}
