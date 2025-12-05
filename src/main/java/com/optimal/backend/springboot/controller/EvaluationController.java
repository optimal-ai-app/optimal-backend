package com.optimal.backend.springboot.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.optimal.backend.springboot.evaluation.AgentEvaluator;
import com.optimal.backend.springboot.evaluation.EvaluationSummary;

/**
 * REST controller for running agent evaluations
 */
@RestController
@RequestMapping("/evaluation")
public class EvaluationController {

    @Autowired
    private AgentEvaluator evaluator;

    /**
     * Run evaluation suite from default test cases file
     */
    @PostMapping("/run")
    public ResponseEntity<Map<String, Object>> runEvaluation(
            @RequestParam(defaultValue = "test-cases.json") String testCasesFile) {
        // File is automatically loaded from resources/evaluation/ folder
        try {
            EvaluationSummary summary = evaluator.runEvaluation(testCasesFile);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("suiteName", summary.getSuiteName());
            response.put("totalTests", summary.getTotalTests());
            response.put("passed", summary.getPassed());
            response.put("failed", summary.getFailed());
            response.put("hitRate", summary.getHitRate());
            response.put("averageDurationMs", summary.getAverageDurationMs());
            response.put("benchmarkId", summary.getBenchmarkId());
            response.put("csvFilePath", summary.getCsvFilePath());
            response.put("jsonFilePath", summary.getJsonFilePath());
            response.put("message", "Evaluation complete. Results saved to benchmarks folder with ID: " + summary.getBenchmarkId());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * Run evaluation with custom test cases (POST body)
     */
    @PostMapping("/run-custom")
    public ResponseEntity<Map<String, Object>> runCustomEvaluation(@RequestBody Map<String, Object> request) {
        // This would require additional implementation to accept test cases in request body
        // For now, use the file-based approach
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Use /evaluation/run with testCasesFile parameter");
        return ResponseEntity.ok(response);
    }

    /**
     * Get evaluation status/info
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getEvaluationInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("description", "Agent Workflow Evaluation System");
        info.put("endpoints", Map.of(
            "run", "POST /evaluation/run?testCasesFile=test-cases.json",
            "info", "GET /evaluation/info"
        ));
        info.put("defaultTestFile", "test-cases.json");
        info.put("outputFile", "evaluation-results.csv");
        return ResponseEntity.ok(info);
    }
}

