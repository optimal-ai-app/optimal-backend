package com.optimal.backend.springboot.evaluation;

import java.util.List;
import java.util.Map;

/**
 * Represents a single test case for agent evaluation
 */
public class TestCase {
    private String id;
    private String description;
    private String input;
    private String expectedAgentTeam; // GoalDefinitionTeam, TaskExecutionTeam, MilestoneExecutionTeam
    private List<String> expectedAgents; // List of agent names that should be selected
    private List<String> expectedTags;
    private List<String> expectedDataKeys;
    private Integer expectedStep;
    private Boolean expectedReadyToHandoff;
    private Boolean expectedReInterpret;
    private Map<String, Object> expectedData;
    private String expectedContentContains; // Substring that should be in content
    private Integer maxExecutionTimeMs; // Maximum allowed execution time

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getExpectedAgentTeam() {
        return expectedAgentTeam;
    }

    public void setExpectedAgentTeam(String expectedAgentTeam) {
        this.expectedAgentTeam = expectedAgentTeam;
    }

    public List<String> getExpectedAgents() {
        return expectedAgents;
    }

    public void setExpectedAgents(List<String> expectedAgents) {
        this.expectedAgents = expectedAgents;
    }

    public List<String> getExpectedTags() {
        return expectedTags;
    }

    public void setExpectedTags(List<String> expectedTags) {
        this.expectedTags = expectedTags;
    }

    public List<String> getExpectedDataKeys() {
        return expectedDataKeys;
    }

    public void setExpectedDataKeys(List<String> expectedDataKeys) {
        this.expectedDataKeys = expectedDataKeys;
    }

    public Integer getExpectedStep() {
        return expectedStep;
    }

    public void setExpectedStep(Integer expectedStep) {
        this.expectedStep = expectedStep;
    }

    public Boolean getExpectedReadyToHandoff() {
        return expectedReadyToHandoff;
    }

    public void setExpectedReadyToHandoff(Boolean expectedReadyToHandoff) {
        this.expectedReadyToHandoff = expectedReadyToHandoff;
    }

    public Boolean getExpectedReInterpret() {
        return expectedReInterpret;
    }

    public void setExpectedReInterpret(Boolean expectedReInterpret) {
        this.expectedReInterpret = expectedReInterpret;
    }

    public Map<String, Object> getExpectedData() {
        return expectedData;
    }

    public void setExpectedData(Map<String, Object> expectedData) {
        this.expectedData = expectedData;
    }

    public String getExpectedContentContains() {
        return expectedContentContains;
    }

    public void setExpectedContentContains(String expectedContentContains) {
        this.expectedContentContains = expectedContentContains;
    }

    public Integer getMaxExecutionTimeMs() {
        return maxExecutionTimeMs;
    }

    public void setMaxExecutionTimeMs(Integer maxExecutionTimeMs) {
        this.maxExecutionTimeMs = maxExecutionTimeMs;
    }
}

