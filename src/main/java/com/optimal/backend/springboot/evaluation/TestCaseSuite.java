package com.optimal.backend.springboot.evaluation;

import java.util.List;

/**
 * Container for a suite of test cases
 */
public class TestCaseSuite {
    private String name;
    private String description;
    private List<TestCase> testCases;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<TestCase> getTestCases() {
        return testCases;
    }

    public void setTestCases(List<TestCase> testCases) {
        this.testCases = testCases;
    }
}

