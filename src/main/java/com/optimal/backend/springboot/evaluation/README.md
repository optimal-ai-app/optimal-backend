# Agent Workflow Evaluation System

A comprehensive evaluation framework for testing multi-agent workflows, supervisor routing, agent handoffs, and step progression.

## Features

- **Workflow-Level Evaluation**: Tests entire agent workflows, not just individual LLM calls
- **Supervisor Routing Validation**: Validates that the supervisor selects the correct agent teams
- **Agent Handoff Testing**: Verifies handoff mechanisms work correctly
- **Step Progression Validation**: Ensures agents follow correct step sequences
- **Hit Rate Calculation**: Measures success rate across test cases
- **CSV Export**: Exports detailed results for analysis

## Usage

### Running Evaluations

#### Via REST API

```bash
# Run default test suite
curl -X POST http://localhost:8080/evaluation/run

# Run specific test file
curl -X POST "http://localhost:8080/evaluation/run?testCasesFile=my-test-cases.json"

# Get evaluation info
curl http://localhost:8080/evaluation/info
```

#### Programmatically

```java
@Autowired
private AgentEvaluator evaluator;

EvaluationSummary summary = evaluator.runEvaluation("test-cases.json");
System.out.println("Hit Rate: " + summary.getHitRate() + "%");
```

## Test Case Format

Test cases are defined in JSON format:

```json
{
  "name": "Test Suite Name",
  "description": "Description of the test suite",
  "testCases": [
    {
      "id": "test_001",
      "description": "Test description",
      "input": "User input message",
      "expectedAgentTeam": "TaskExecutionTeam",
      "expectedAgents": ["TaskPlannerAgent", "TaskCreatorAgent"],
      "expectedTags": ["CONFIRM_TAG"],
      "expectedDataKeys": ["options"],
      "expectedStep": 1,
      "expectedReadyToHandoff": false,
      "expectedReInterpret": null,
      "expectedContentContains": "goal",
      "maxExecutionTimeMs": 30000
    }
  ]
}
```

### Test Case Fields

- **id**: Unique identifier for the test case
- **description**: Human-readable description
- **input**: User input message to test
- **expectedAgentTeam**: Expected agent team (GoalDefinitionTeam, TaskExecutionTeam, MilestoneExecutionTeam)
- **expectedAgents**: List of expected agent names
- **expectedTags**: Expected tags in response
- **expectedDataKeys**: Expected keys in response data object
- **expectedStep**: Expected current step number
- **expectedReadyToHandoff**: Expected readyToHandoff value
- **expectedReInterpret**: Expected reInterpret value
- **expectedContentContains**: Substring that should be in content
- **maxExecutionTimeMs**: Maximum allowed execution time

## Evaluation Metrics

The evaluation system calculates:

- **Hit Rate**: Percentage of tests that passed
- **Average Duration**: Average execution time per test
- **Pass/Fail Count**: Total passed and failed tests
- **Validation Errors**: Detailed error messages for failed tests

## Output

### Console Output

The evaluator prints:
- Test execution progress
- Pass/fail status for each test
- Validation errors
- Summary statistics

### CSV Export

Results are exported to `evaluation-results.csv` with columns:
- Test ID
- Description
- Passed (PASS/FAIL)
- Duration (ms)
- Error message
- Validation errors
- Actual agent team
- Actual agents
- Actual step
- Actual readyToHandoff

## Architecture

### Components

1. **TestCase**: Model for a single test case
2. **EvaluationResult**: Result of evaluating a test case
3. **EvaluationSummary**: Summary of all test results
4. **AgentEvaluator**: Main evaluation engine
5. **EvaluationController**: REST API for running evaluations

### Evaluation Flow

1. Load test cases from JSON file
2. For each test case:
   - Create isolated supervisor instance
   - Execute test input through supervisor
   - Capture actual response and agent selection
   - Validate against expectations
   - Record results
3. Calculate summary statistics
4. Export results to CSV

## Adding Test Cases

Add test cases to `src/main/resources/evaluation/test-cases.json`:

```json
{
  "testCases": [
    {
      "id": "my_test_001",
      "description": "My test case",
      "input": "Test input",
      "expectedAgentTeam": "TaskExecutionTeam",
      ...
    }
  ]
}
```

## Custom Validation

The evaluator validates:
- Agent team selection
- Specific agent selection
- Response tags
- Response data keys
- Step progression
- Handoff readiness
- Re-interpretation triggers
- Content substrings
- Execution time limits

## Notes

- Each test case runs in isolation with a fresh supervisor instance
- Test execution uses fixed date (2024-01-15) for reproducibility
- User context is cleared after each test
- Results are exported to CSV for further analysis

