package com.optimal.backend.springboot.evaluation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.optimal.backend.springboot.agent.framework.agents.GoalCreatorAgent;
import com.optimal.backend.springboot.agent.framework.agents.MilestonePlannerAgent;
import com.optimal.backend.springboot.agent.framework.agents.MilestoneTaskCreatorAgent;
import com.optimal.backend.springboot.agent.framework.agents.TaskCreatorAgent;
import com.optimal.backend.springboot.agent.framework.agents.TaskPlannerAgent;
import com.optimal.backend.springboot.agent.framework.core.BaseSupervisor;
import com.optimal.backend.springboot.agent.framework.core.LlmClient;
import com.optimal.backend.springboot.agent.framework.core.Message;
import com.optimal.backend.springboot.agent.framework.core.UserContext;

/**
 * Evaluates agent workflows by running test cases and validating results
 */
@Component
public class AgentEvaluator {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private LlmClient llmClient;

    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Run evaluation suite from a test cases JSON file
     */
    public EvaluationSummary runEvaluation(String testCasesPath) throws IOException {
        // Load test cases from evaluation folder
        String evaluationPath = "evaluation/" + testCasesPath;
        TestCaseSuite suite = mapper.readValue(
                new ClassPathResource(evaluationPath).getInputStream(),
                TestCaseSuite.class);

        System.out.println("\n=== Starting Evaluation: " + suite.getName() + " ===");
        System.out.println("Description: " + suite.getDescription());
        System.out.println("Test Cases: " + suite.getTestCases().size() + "\n");

        List<EvaluationResult> results = new ArrayList<>();

        for (TestCase testCase : suite.getTestCases()) {
            System.out.println("Running test: " + testCase.getId() + " - " + testCase.getDescription());
            EvaluationResult result = evaluateTestCase(testCase);
            results.add(result);
            System.out.println("  Result: " + (result.isPassed() ? "PASS" : "FAIL"));
            if (!result.isPassed() && !result.getValidationErrors().isEmpty()) {
                System.out.println("  Errors: " + String.join(", ", result.getValidationErrors()));
            }
            System.out.println("  Duration: " + result.getDurationMs() + "ms\n");
        }

        // Calculate summary
        EvaluationSummary summary = calculateSummary(results, suite);
        printSummary(summary);
        
        // Generate benchmark ID and save results
        String benchmarkId = generateBenchmarkId();
        String benchmarksDir = "benchmarks";
        ensureBenchmarksDirectory(benchmarksDir);
        
        // Set benchmark ID and file paths in summary before export
        summary.setBenchmarkId(benchmarkId);
        String csvFilename = String.format("%s/benchmark-%s.csv", benchmarksDir, benchmarkId);
        String jsonFilename = String.format("%s/benchmark-%s-summary.json", benchmarksDir, benchmarkId);
        summary.setCsvFilePath(csvFilename);
        summary.setJsonFilePath(jsonFilename);
        
        // Export to CSV with benchmark ID
        exportToCSV(results, csvFilename);
        
        // Export summary JSON
        exportSummaryToJson(summary, jsonFilename, benchmarkId);

        return summary;
    }

    /**
     * Evaluate a single test case
     */
    private EvaluationResult evaluateTestCase(TestCase testCase) {
        EvaluationResult result = new EvaluationResult();
        result.setTestId(testCase.getId());
        result.setDescription(testCase.getDescription());

        long startTime = System.currentTimeMillis();
        UUID testUserId = UUID.randomUUID();
        UUID testChatId = UUID.randomUUID();

        try {
            // Set up test context
            UserContext.setUserId(testUserId);
            UserContext.setChatId(testChatId);
            UserContext.setUserDate("2024-01-15"); // Fixed date for reproducibility

            // Create supervisor for this test
            BaseSupervisor supervisor = createTestSupervisor();

            // Prepare input messages
            List<Message> inputMessages = new ArrayList<>();
            inputMessages.add(new Message("system", "User ID: " + testUserId.toString()));
            inputMessages.add(new Message("system", "Date: 2024-01-15"));
            inputMessages.add(new Message("user", testCase.getInput()));

            // Capture agent selection before execution (interpret is called inside executeWithHandoff)
            // We'll capture it after execution by checking what agents were actually used
            // For now, we'll interpret separately to capture selection, then execute
            supervisor.interpret(inputMessages);
            List<String> selectedAgents = supervisor.getSelectedAgentNames();
            result.setActualAgents(selectedAgents);
            result.setActualAgentTeam(determineAgentTeam(selectedAgents));

            // Execute supervisor
            BaseSupervisor.SupervisorResponse response = supervisor.executeWithHandoff(inputMessages);

            long duration = System.currentTimeMillis() - startTime;

            // Check execution time
            if (testCase.getMaxExecutionTimeMs() != null && duration > testCase.getMaxExecutionTimeMs()) {
                result.addValidationError("Execution time exceeded: " + duration + "ms > " + testCase.getMaxExecutionTimeMs() + "ms");
            }

            // Capture actual values
            result.setDurationMs(duration);
            result.setActualContent(response.content);
            result.setActualTags(response.tags);
            result.setActualStep(response.currentStep);
            result.setActualReadyToHandoff(response.readyToHandoff);
            result.setActualReInterpret(response.reInterpret);

            // Validate response
            validateResponse(result, testCase, response);

            result.setPassed(result.getValidationErrors().isEmpty());

        } catch (Exception e) {
            result.setPassed(false);
            result.setError(e.getMessage());
            result.setDurationMs(System.currentTimeMillis() - startTime);
            result.addValidationError("Exception: " + e.getMessage());
            e.printStackTrace();
        } finally {
            UserContext.clear();
        }

        return result;
    }

    /**
     * Validate response against test case expectations
     */
    private void validateResponse(EvaluationResult result, TestCase testCase, 
                                  BaseSupervisor.SupervisorResponse response) {
        
        // Validate agent team selection
        if (testCase.getExpectedAgentTeam() != null) {
            if (!testCase.getExpectedAgentTeam().equals(result.getActualAgentTeam())) {
                result.addValidationError("Agent team mismatch: expected " + testCase.getExpectedAgentTeam() + 
                                         ", got " + result.getActualAgentTeam());
            }
        }

        // Validate specific agents
        if (testCase.getExpectedAgents() != null && !testCase.getExpectedAgents().isEmpty()) {
            List<String> actualAgents = result.getActualAgents();
            for (String expectedAgent : testCase.getExpectedAgents()) {
                if (!actualAgents.contains(expectedAgent)) {
                    result.addValidationError("Missing expected agent: " + expectedAgent);
                }
            }
        }

        // Validate tags
        if (testCase.getExpectedTags() != null && !testCase.getExpectedTags().isEmpty()) {
            for (String expectedTag : testCase.getExpectedTags()) {
                if (!response.tags.contains(expectedTag)) {
                    result.addValidationError("Missing expected tag: " + expectedTag);
                }
            }
        }

        // Validate data keys
        if (testCase.getExpectedDataKeys() != null && !testCase.getExpectedDataKeys().isEmpty()) {
            if (response.data == null) {
                result.addValidationError("Response data is null but expected keys: " + testCase.getExpectedDataKeys());
            } else {
                for (String expectedKey : testCase.getExpectedDataKeys()) {
                    if (!response.data.containsKey(expectedKey)) {
                        result.addValidationError("Missing expected data key: " + expectedKey);
                    }
                }
            }
        }

        // Validate step
        if (testCase.getExpectedStep() != null) {
            if (response.currentStep != testCase.getExpectedStep()) {
                result.addValidationError("Step mismatch: expected " + testCase.getExpectedStep() + 
                                         ", got " + response.currentStep);
            }
        }

        // Validate readyToHandoff
        if (testCase.getExpectedReadyToHandoff() != null) {
            if (response.readyToHandoff != testCase.getExpectedReadyToHandoff()) {
                result.addValidationError("readyToHandoff mismatch: expected " + testCase.getExpectedReadyToHandoff() + 
                                         ", got " + response.readyToHandoff);
            }
        }

        // Validate reInterpret
        if (testCase.getExpectedReInterpret() != null) {
            if (response.reInterpret != testCase.getExpectedReInterpret()) {
                result.addValidationError("reInterpret mismatch: expected " + testCase.getExpectedReInterpret() + 
                                         ", got " + response.reInterpret);
            }
        }

        // Validate content contains substring
        if (testCase.getExpectedContentContains() != null) {
            if (response.content == null || !response.content.contains(testCase.getExpectedContentContains())) {
                result.addValidationError("Content does not contain expected substring: " + testCase.getExpectedContentContains());
            }
        }

        // Validate JSON structure (basic check - OutputValidationGuard should handle this)
        if (response.content == null || response.content.trim().isEmpty()) {
            result.addValidationError("Response content is null or empty");
        }
    }

    /**
     * Determine agent team from list of agent names
     */
    private String determineAgentTeam(List<String> agentNames) {
        if (agentNames == null || agentNames.isEmpty()) {
            return "None";
        }
        
        // Check for GoalDefinitionTeam
        if (agentNames.contains("GoalCreatorAgent") && agentNames.size() == 1) {
            return "GoalDefinitionTeam";
        }
        
        // Check for TaskExecutionTeam
        if (agentNames.contains("TaskPlannerAgent") && agentNames.contains("TaskCreatorAgent")) {
            return "TaskExecutionTeam";
        }
        
        // Check for MilestoneExecutionTeam
        if (agentNames.contains("MilestonePlannerAgent") && agentNames.contains("MilestoneTaskCreatorAgent")) {
            return "MilestoneExecutionTeam";
        }
        
        return "Unknown";
    }

    /**
     * Create a test supervisor with all agents
     */
    private BaseSupervisor createTestSupervisor() {
        TaskPlannerAgent taskPlannerAgent = applicationContext.getBean(TaskPlannerAgent.class);
        TaskCreatorAgent taskCreatorAgent = applicationContext.getBean(TaskCreatorAgent.class);
        MilestoneTaskCreatorAgent milestoneTaskCreatorAgent = applicationContext.getBean(MilestoneTaskCreatorAgent.class);
        GoalCreatorAgent goalCreatorAgent = applicationContext.getBean(GoalCreatorAgent.class);
        MilestonePlannerAgent milestonePlannerAgent = applicationContext.getBean(MilestonePlannerAgent.class);

        BaseSupervisor supervisor = new BaseSupervisor(llmClient);
        supervisor.addAgent(taskPlannerAgent.getName(), taskPlannerAgent);
        supervisor.addAgent(taskCreatorAgent.getName(), taskCreatorAgent);
        supervisor.addAgent(milestoneTaskCreatorAgent.getName(), milestoneTaskCreatorAgent);
        supervisor.addAgent(goalCreatorAgent.getName(), goalCreatorAgent);
        supervisor.addAgent(milestonePlannerAgent.getName(), milestonePlannerAgent);
        // Don't set ChatService for evaluation - we don't want to save to database during testing
        // supervisor.setChatService(chatService);

        return supervisor;
    }

    /**
     * Calculate evaluation summary
     */
    private EvaluationSummary calculateSummary(List<EvaluationResult> results, TestCaseSuite suite) {
        EvaluationSummary summary = new EvaluationSummary();
        summary.setSuiteName(suite.getName());
        summary.setTotalTests(results.size());
        
        long passed = results.stream().filter(EvaluationResult::isPassed).count();
        summary.setPassed((int) passed);
        summary.setFailed(results.size() - (int) passed);
        
        if (results.size() > 0) {
            summary.setHitRate((double) passed / results.size() * 100);
        }
        
        double avgDuration = results.stream()
                .mapToLong(EvaluationResult::getDurationMs)
                .average()
                .orElse(0.0);
        summary.setAverageDurationMs((long) avgDuration);
        
        summary.setResults(results);
        
        return summary;
    }

    /**
     * Print evaluation summary
     */
    private void printSummary(EvaluationSummary summary) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("EVALUATION SUMMARY");
        System.out.println("=".repeat(60));
        System.out.println("Suite: " + summary.getSuiteName());
        System.out.println("Total Tests: " + summary.getTotalTests());
        System.out.println("Passed: " + summary.getPassed());
        System.out.println("Failed: " + summary.getFailed());
        System.out.println("Hit Rate: " + String.format("%.2f%%", summary.getHitRate()));
        System.out.println("Average Duration: " + summary.getAverageDurationMs() + "ms");
        System.out.println("=".repeat(60) + "\n");
    }

    /**
     * Export results to CSV
     */
    private void exportToCSV(List<EvaluationResult> results, String filename) {
        try (FileWriter writer = new FileWriter(filename)) {
            // Header
            writer.append("Test ID,Description,Passed,Duration (ms),Error,Validation Errors,Actual Agent Team,Actual Agents,Actual Step,Actual ReadyToHandoff\n");
            
            // Data rows
            for (EvaluationResult result : results) {
                writer.append(String.format("%s,%s,%s,%d,%s,%s,%s,%s,%s,%s\n",
                    escapeCSV(result.getTestId()),
                    escapeCSV(result.getDescription()),
                    result.isPassed() ? "PASS" : "FAIL",
                    result.getDurationMs(),
                    escapeCSV(result.getError() != null ? result.getError() : ""),
                    escapeCSV(String.join("; ", result.getValidationErrors())),
                    escapeCSV(result.getActualAgentTeam() != null ? result.getActualAgentTeam() : ""),
                    escapeCSV(result.getActualAgents() != null ? String.join("; ", result.getActualAgents()) : ""),
                    result.getActualStep() != null ? result.getActualStep().toString() : "",
                    result.getActualReadyToHandoff() != null ? result.getActualReadyToHandoff().toString() : ""
                ));
            }
            
            System.out.println("CSV results exported to: " + filename);
        } catch (IOException e) {
            System.err.println("Failed to export CSV: " + e.getMessage());
        }
    }

    private String escapeCSV(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    /**
     * Generate a unique benchmark ID with timestamp
     */
    private String generateBenchmarkId() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        return timestamp + "-" + uuid;
    }

    /**
     * Ensure benchmarks directory exists
     */
    private void ensureBenchmarksDirectory(String dirPath) {
        File dir = new File(dirPath);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (created) {
                System.out.println("Created benchmarks directory: " + dirPath);
            } else {
                System.err.println("Failed to create benchmarks directory: " + dirPath);
            }
        }
    }

    /**
     * Export summary to JSON file
     */
    private void exportSummaryToJson(EvaluationSummary summary, String filename, String benchmarkId) {
        try {
            ObjectMapper jsonMapper = new ObjectMapper();
            jsonMapper.enable(SerializationFeature.INDENT_OUTPUT);
            
            // Create a summary object with benchmark metadata
            Map<String, Object> summaryData = new HashMap<>();
            summaryData.put("benchmarkId", benchmarkId);
            summaryData.put("timestamp", LocalDateTime.now().toString());
            summaryData.put("suiteName", summary.getSuiteName());
            summaryData.put("totalTests", summary.getTotalTests());
            summaryData.put("passed", summary.getPassed());
            summaryData.put("failed", summary.getFailed());
            summaryData.put("hitRate", summary.getHitRate());
            summaryData.put("averageDurationMs", summary.getAverageDurationMs());
            summaryData.put("csvFile", summary.getCsvFilePath());
            summaryData.put("jsonFile", summary.getJsonFilePath());
            
            jsonMapper.writeValue(new File(filename), summaryData);
            System.out.println("Summary exported to: " + filename);
        } catch (IOException e) {
            System.err.println("Failed to export JSON summary: " + e.getMessage());
        }
    }
}

