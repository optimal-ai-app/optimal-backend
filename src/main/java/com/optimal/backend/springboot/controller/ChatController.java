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
import com.optimal.backend.springboot.agent.framework.core.UserContext;

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
        try {
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

            // Set userId in ThreadLocal context for tools to access
            UserContext.setUserId(userId);
            System.out.println("=== ChatController: Set userId in context: " + userId);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> messages = (List<Map<String, Object>>) request.get("messages");

            // Create a new list with userId as system message at the front
            List<Message> convertedMessages = new ArrayList<>();

            // Add userId as system message at the front (keeping for backward
            // compatibility)
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
                newSupervisor.addAgent(taskAgent.getName(), taskAgent);
                return newSupervisor;
            });

            // Execute supervisor with handoff support
            BaseSupervisor.SupervisorResponse supervisorResponse = userSupervisor.executeWithHandoff(convertedMessages);

            // Convert to the expected response format
            Map<String, Object> response = new HashMap<>();
            response.put("content", supervisorResponse.content);
            response.put("tags", supervisorResponse.tags);
            response.put("readyToHandoff", supervisorResponse.readyToHandoff);
            response.put("data", supervisorResponse.data);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("Error in ChatController: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("content", "Sorry, I encountered an error. Please try again.");
            errorResponse.put("tags", new ArrayList<>());
            errorResponse.put("readyToHandoff", false);
            errorResponse.put("data", new HashMap<>());
            return ResponseEntity.status(500).body(errorResponse);

        } finally {
            // Always clear the user context to prevent memory leaks
            UserContext.clear();
            System.out.println("=== ChatController: Cleared userId from context");
        }
    }
}
