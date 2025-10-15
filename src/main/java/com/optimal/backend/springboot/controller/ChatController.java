package com.optimal.backend.springboot.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.UUID;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.annotation.PreDestroy;

import com.optimal.backend.springboot.agent.framework.agents.TaskCreatorAgent;
import com.optimal.backend.springboot.agent.framework.agents.MilestonePlannerAgent;
import com.optimal.backend.springboot.agent.framework.agents.TaskPlannerAgent;
import com.optimal.backend.springboot.agent.framework.agents.GoalCreatorAgent;
import com.optimal.backend.springboot.agent.framework.core.BaseSupervisor;
import com.optimal.backend.springboot.agent.framework.core.LlmClient;
import com.optimal.backend.springboot.agent.framework.core.Message;
import com.optimal.backend.springboot.agent.framework.core.UserContext;
import com.optimal.backend.springboot.service.ChatService;

@RestController
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private LlmClient llmClient;
    final Integer MESSAGE_MAXIMUM = 100;

    // Wrapper class to track supervisor access time
    private static class SupervisorWrapper {
        private final BaseSupervisor supervisor;
        private volatile LocalDateTime lastAccessed;

        public SupervisorWrapper(BaseSupervisor supervisor) {
            this.supervisor = supervisor;
            this.lastAccessed = LocalDateTime.now();
        }

        public BaseSupervisor getSupervisor() {
            this.lastAccessed = LocalDateTime.now();
            return supervisor;
        }

        public LocalDateTime getLastAccessed() {
            return lastAccessed;
        }

        public boolean isExpired(int inactivityMinutes) {
            return ChronoUnit.MINUTES.between(lastAccessed, LocalDateTime.now()) >= inactivityMinutes;
        }
    }

    // Map to store user-specific supervisors with access tracking
    private final Map<String, SupervisorWrapper> userSupervisors = new ConcurrentHashMap<>();

    // Configuration constants
    private static final int SUPERVISOR_INACTIVITY_MINUTES = 30;
    private static final int MAX_SUPERVISORS = 150;
    private static final int CLEANUP_INTERVAL_MINUTES = 10;

    // Background cleanup executor
    private final ScheduledExecutorService cleanupExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "supervisor-cleanup");
        System.out.println("Starting supervisor cleanup executor");
        t.setDaemon(true);
        return t;
    });

    // Metrics tracking
    private volatile long totalSupervisorsCreated = 0;
    private volatile long totalSupervisorsEvicted = 0;
    private volatile LocalDateTime lastCleanupTime = LocalDateTime.now();

    // Initialize cleanup task
    {
        cleanupExecutor.scheduleAtFixedRate(
                this::cleanupInactiveSupervisors,
                CLEANUP_INTERVAL_MINUTES,
                CLEANUP_INTERVAL_MINUTES,
                TimeUnit.MINUTES);
    }

    /**
     * Cleanup inactive supervisors based on time and size limits
     */
    private void cleanupInactiveSupervisors() {
        try {
            int initialSize = userSupervisors.size();
            int removedCount = 0;

            // Remove expired supervisors
            Iterator<Map.Entry<String, SupervisorWrapper>> iterator = userSupervisors.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, SupervisorWrapper> entry = iterator.next();
                SupervisorWrapper wrapper = entry.getValue();

                if (wrapper.isExpired(SUPERVISOR_INACTIVITY_MINUTES)) {
                    cleanupSupervisor(wrapper.getSupervisor());
                    iterator.remove();
                    removedCount++;
                }
            }

            // If still over capacity, remove oldest supervisors (LRU-style)
            if (userSupervisors.size() > MAX_SUPERVISORS) {
                removedCount += evictOldestSupervisors(userSupervisors.size() - MAX_SUPERVISORS);
            }

            if (removedCount > 0) {
                System.out.printf("[SUPERVISOR-GC] Cleaned up %d supervisors (before: %d, after: %d)%n",
                        removedCount, initialSize, userSupervisors.size());
                totalSupervisorsEvicted += removedCount;
            }
            lastCleanupTime = LocalDateTime.now();
        } catch (Exception e) {
            System.err.println("[SUPERVISOR-GC] Error during cleanup: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Evict oldest supervisors when over capacity
     */
    private int evictOldestSupervisors(int countToRemove) {
        return userSupervisors.entrySet().stream()
                .sorted(Map.Entry.<String, SupervisorWrapper>comparingByValue(
                        (a, b) -> a.getLastAccessed().compareTo(b.getLastAccessed())))
                .limit(countToRemove)
                .mapToInt(entry -> {
                    cleanupSupervisor(entry.getValue().getSupervisor());
                    userSupervisors.remove(entry.getKey());
                    return 1;
                })
                .sum();
    }

    /**
     * Cleanup supervisor internal state to prevent memory leaks
     */
    private void cleanupSupervisor(BaseSupervisor supervisor) {
        try {
            supervisor.clearAllState();
        } catch (Exception e) {
            System.err.println("[SUPERVISOR-GC] Error cleaning supervisor: " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> chat(@RequestBody Map<String, Object> request) {
        try {
            // Extract request parameters
            String date = (String) request.get("date"); // User's local date (yyyy-MM-dd)
            String timestamp = (String) request.get("timestamp"); // Full UTC timestamp for logging
            String chatId = (String) request.get("chatId");

            String userId = (String) request.get("userId");
            long messagesSentToday = chatService.countUsersMessages(userId);
            if (messagesSentToday > MESSAGE_MAXIMUM) {
                Map<String, Object> resp = new HashMap();
                resp.put("content", "Maximum requests used today");
                return ResponseEntity.ok(resp);
            }

            if (chatId == null || chatId.trim().isEmpty() || userId == null || userId.trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("content", "chatId and userId are required");
                errorResponse.put("tags", new ArrayList<>());
                errorResponse.put("readyToHandoff", false);
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // Set context in ThreadLocal for tools to access
            UserContext.setUserId(userId);
            UserContext.setChatId(chatId);
            UserContext.setUserDate(date); // Store user's local date for date calculations

            @SuppressWarnings("unchecked")
            // Extract messages from request - each message has a role (e.g. user/assistant)
            // and content
            List<Map<String, Object>> messages = (List<Map<String, Object>>) request.get("messages");

            chatService.addUserMessage(UUID.fromString(chatId), UUID.fromString(userId),
                    (String) messages.get(messages.size() - 1).get("content"));
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
            SupervisorWrapper supervisorWrapper = userSupervisors.computeIfAbsent(chatId, id -> {

                TaskPlannerAgent taskPlannerAgent = applicationContext.getBean(TaskPlannerAgent.class);
                TaskCreatorAgent taskCreatorAgent = applicationContext.getBean(TaskCreatorAgent.class);
                GoalCreatorAgent goalCreatorAgent = applicationContext.getBean(GoalCreatorAgent.class);
                MilestonePlannerAgent milestonePlannerAgent = applicationContext.getBean(MilestonePlannerAgent.class);
                // HabitAgent habitAgent = new HabitAgent(llmClient);

                BaseSupervisor newSupervisor = new BaseSupervisor(llmClient);
                newSupervisor.addAgent(taskPlannerAgent.getName(), taskPlannerAgent);
                newSupervisor.addAgent(taskCreatorAgent.getName(), taskCreatorAgent);
                newSupervisor.addAgent(goalCreatorAgent.getName(), goalCreatorAgent);
                newSupervisor.addAgent(milestonePlannerAgent.getName(), milestonePlannerAgent);
                // newSupervisor.addAgent(habitAgent.getName(), habitAgent);
                newSupervisor.setChatService(chatService);

                System.out.printf("[SUPERVISOR-GC] Created new supervisor for chatId: %s (total: %d)%n",
                        chatId, userSupervisors.size() + 1);
                totalSupervisorsCreated++;
                return new SupervisorWrapper(newSupervisor);
            });

            BaseSupervisor userSupervisor = supervisorWrapper.getSupervisor();

            // Execute supervisor with handoff support
            BaseSupervisor.SupervisorResponse supervisorResponse = userSupervisor.executeWithHandoff(convertedMessages);

            // Check if processing is complete and clean up immediately
            if (userSupervisor.isProcessingComplete()) {
                System.out.printf("[SUPERVISOR-GC] Processing complete for chatId: %s, cleaning up immediately%n",
                        chatId);
                cleanupSupervisor(userSupervisor);
                userSupervisors.remove(chatId);
                totalSupervisorsEvicted++;
            }

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

    /**
     * Endpoint to view supervisor garbage collection metrics
     */
    @GetMapping("/supervisor-metrics")
    public ResponseEntity<Map<String, Object>> getSupervisorMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("activeSupervisors", userSupervisors.size());
        metrics.put("totalSupervisorsCreated", totalSupervisorsCreated);
        metrics.put("totalSupervisorsEvicted", totalSupervisorsEvicted);
        metrics.put("lastCleanupTime", lastCleanupTime.toString());
        metrics.put("inactivityThresholdMinutes", SUPERVISOR_INACTIVITY_MINUTES);
        metrics.put("maxSupervisors", MAX_SUPERVISORS);
        metrics.put("cleanupIntervalMinutes", CLEANUP_INTERVAL_MINUTES);

        // Calculate some derived metrics
        long memoryEfficiency = totalSupervisorsCreated > 0 ? (totalSupervisorsEvicted * 100) / totalSupervisorsCreated
                : 0;
        metrics.put("memoryEfficiencyPercent", memoryEfficiency);

        return ResponseEntity.ok(metrics);
    }

    /**
     * Manual cleanup endpoint for testing/administrative purposes
     */
    @PostMapping("/cleanup-supervisors")
    public ResponseEntity<Map<String, Object>> manualCleanup() {
        int beforeCount = userSupervisors.size();
        cleanupInactiveSupervisors();
        int afterCount = userSupervisors.size();

        Map<String, Object> result = new HashMap<>();
        result.put("supervisorsBeforeCleanup", beforeCount);
        result.put("supervisorsAfterCleanup", afterCount);
        result.put("supervisorsRemoved", beforeCount - afterCount);
        result.put("cleanupTime", LocalDateTime.now().toString());

        return ResponseEntity.ok(result);
    }

    /**
     * Shutdown cleanup executor when the controller is destroyed
     */
    @PreDestroy
    public void shutdown() {
        try {
            System.out.println("[SUPERVISOR-GC] Shutting down cleanup executor...");
            cleanupExecutor.shutdown();
            if (!cleanupExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                cleanupExecutor.shutdownNow();
            }
            // Cleanup all remaining supervisors
            userSupervisors.values().forEach(wrapper -> cleanupSupervisor(wrapper.getSupervisor()));
            userSupervisors.clear();
            System.out.println("[SUPERVISOR-GC] Cleanup executor shut down successfully");
        } catch (InterruptedException e) {
            cleanupExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
