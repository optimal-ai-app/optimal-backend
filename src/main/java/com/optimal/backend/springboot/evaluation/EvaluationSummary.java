package com.optimal.backend.springboot.evaluation;

import java.util.List;

/**
 * Summary of evaluation results
 */
public class EvaluationSummary {
    private String suiteName;
    private int totalTests;
    private int passed;
    private int failed;
    private double hitRate;
    private long averageDurationMs;
    private List<EvaluationResult> results;
    private String benchmarkId;
    private String csvFilePath;
    private String jsonFilePath;

    // Getters and setters
    public String getSuiteName() {
        return suiteName;
    }

    public void setSuiteName(String suiteName) {
        this.suiteName = suiteName;
    }

    public int getTotalTests() {
        return totalTests;
    }

    public void setTotalTests(int totalTests) {
        this.totalTests = totalTests;
    }

    public int getPassed() {
        return passed;
    }

    public void setPassed(int passed) {
        this.passed = passed;
    }

    public int getFailed() {
        return failed;
    }

    public void setFailed(int failed) {
        this.failed = failed;
    }

    public double getHitRate() {
        return hitRate;
    }

    public void setHitRate(double hitRate) {
        this.hitRate = hitRate;
    }

    public long getAverageDurationMs() {
        return averageDurationMs;
    }

    public void setAverageDurationMs(long averageDurationMs) {
        this.averageDurationMs = averageDurationMs;
    }

    public List<EvaluationResult> getResults() {
        return results;
    }

    public void setResults(List<EvaluationResult> results) {
        this.results = results;
    }

    public String getBenchmarkId() {
        return benchmarkId;
    }

    public void setBenchmarkId(String benchmarkId) {
        this.benchmarkId = benchmarkId;
    }

    public String getCsvFilePath() {
        return csvFilePath;
    }

    public void setCsvFilePath(String csvFilePath) {
        this.csvFilePath = csvFilePath;
    }

    public String getJsonFilePath() {
        return jsonFilePath;
    }

    public void setJsonFilePath(String jsonFilePath) {
        this.jsonFilePath = jsonFilePath;
    }
}

