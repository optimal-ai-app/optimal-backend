package com.optimal.backend.springboot.evaluation;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the result of evaluating a single test case
 */
public class EvaluationResult {
    private String testId;
    private String description;
    private boolean passed;
    private long durationMs;
    private String error;
    private List<String> validationErrors;
    private String actualAgentTeam;
    private List<String> actualAgents;
    private String actualContent;
    private List<String> actualTags;
    private Integer actualStep;
    private Boolean actualReadyToHandoff;
    private Boolean actualReInterpret;

    public EvaluationResult() {
        this.validationErrors = new ArrayList<>();
    }

    public EvaluationResult(String testId, boolean passed, long durationMs, String error) {
        this.testId = testId;
        this.passed = passed;
        this.durationMs = durationMs;
        this.error = error;
        this.validationErrors = new ArrayList<>();
    }

    public void addValidationError(String error) {
        this.validationErrors.add(error);
    }

    // Getters and setters
    public String getTestId() {
        return testId;
    }

    public void setTestId(String testId) {
        this.testId = testId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isPassed() {
        return passed;
    }

    public void setPassed(boolean passed) {
        this.passed = passed;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(long durationMs) {
        this.durationMs = durationMs;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public List<String> getValidationErrors() {
        return validationErrors;
    }

    public void setValidationErrors(List<String> validationErrors) {
        this.validationErrors = validationErrors;
    }

    public String getActualAgentTeam() {
        return actualAgentTeam;
    }

    public void setActualAgentTeam(String actualAgentTeam) {
        this.actualAgentTeam = actualAgentTeam;
    }

    public List<String> getActualAgents() {
        return actualAgents;
    }

    public void setActualAgents(List<String> actualAgents) {
        this.actualAgents = actualAgents;
    }

    public String getActualContent() {
        return actualContent;
    }

    public void setActualContent(String actualContent) {
        this.actualContent = actualContent;
    }

    public List<String> getActualTags() {
        return actualTags;
    }

    public void setActualTags(List<String> actualTags) {
        this.actualTags = actualTags;
    }

    public Integer getActualStep() {
        return actualStep;
    }

    public void setActualStep(Integer actualStep) {
        this.actualStep = actualStep;
    }

    public Boolean getActualReadyToHandoff() {
        return actualReadyToHandoff;
    }

    public void setActualReadyToHandoff(Boolean actualReadyToHandoff) {
        this.actualReadyToHandoff = actualReadyToHandoff;
    }

    public Boolean getActualReInterpret() {
        return actualReInterpret;
    }

    public void setActualReInterpret(Boolean actualReInterpret) {
        this.actualReInterpret = actualReInterpret;
    }
}

