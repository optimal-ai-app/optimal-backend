# Tool Specification Guide for LangChain4j Integration

This guide explains how to create tools that work seamlessly with LangChain4j's tool calling system.

## Tool Interface Overview

The enhanced `Tool` interface now supports structured parameters via LangChain4j's `ToolParameters`:

```java
public interface Tool {
    String getName();                    // Tool identifier
    String execute(String input);       // Executes with JSON input
    String getDescription();             // Human-readable description
    ToolParameters getParameters();      // Parameter schema (NEW!)
}
```

## Creating Tool Parameters

### 1. Basic Parameter Types

```java
@Override
public ToolParameters getParameters() {
    return ToolParameters.builder()
            .type("object")
            .properties(Map.of(
                "name", Map.of("type", "string", "description", "User's name"),
                "age", Map.of("type", "number", "description", "User's age"),
                "active", Map.of("type", "boolean", "description", "Is user active")
            ))
            .required(Arrays.asList("name"))  // Only name is required
            .build();
}
```

### 2. Enum Parameters

```java
"status", Map.of(
    "type", "string",
    "description", "Current status",
    "enum", Arrays.asList("pending", "active", "completed", "cancelled")
)
```

### 3. Complex Example: Goal Management Tool

```java
@Override
public ToolParameters getParameters() {
    return ToolParameters.builder()
            .type("object")
            .properties(Map.of(
                "title", Map.of(
                    "type", "string",
                    "description", "The goal title (required)"
                ),
                "description", Map.of(
                    "type", "string",
                    "description", "Detailed description of the goal"
                ),
                "category", Map.of(
                    "type", "string",
                    "description", "Goal category",
                    "enum", Arrays.asList("personal", "work", "health", "education", "finance")
                ),
                "priority", Map.of(
                    "type", "string",
                    "description", "Priority level",
                    "enum", Arrays.asList("low", "medium", "high", "critical")
                ),
                "deadline", Map.of(
                    "type", "string",
                    "description", "Target completion date (YYYY-MM-DD format)"
                ),
                "targetValue", Map.of(
                    "type", "number",
                    "description", "Numeric target (e.g., weight, savings amount)"
                ),
                "isRecurring", Map.of(
                    "type", "boolean",
                    "description", "Whether this is a recurring goal"
                )
            ))
            .required(Arrays.asList("title"))
            .build();
}
```

## Parsing JSON Input

Tools receive JSON strings and should parse them properly:

```java
@Override
public String execute(String input) {
    try {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode inputNode = mapper.readTree(input);

        // Extract required parameters
        String title = inputNode.get("title").asText();

        // Extract optional parameters with defaults
        String category = inputNode.has("category") ?
            inputNode.get("category").asText() : "personal";
        int priority = inputNode.has("priority") ?
            inputNode.get("priority").asInt() : 1;
        boolean isActive = inputNode.has("isActive") ?
            inputNode.get("isActive").asBoolean() : true;

        // Process and return result
        return processGoal(title, category, priority, isActive);

    } catch (Exception e) {
        return "Error: " + e.getMessage();
    }
}
```

## Best Practices

### 1. Parameter Validation

```java
// Validate required parameters
if (title == null || title.trim().isEmpty()) {
    return "Error: Title is required and cannot be empty.";
}

// Validate enum values
List<String> validCategories = Arrays.asList("personal", "work", "health");
if (!validCategories.contains(category)) {
    return "Error: Invalid category. Must be one of: " + validCategories;
}
```

### 2. Meaningful Descriptions

```java
"deadline", Map.of(
    "type", "string",
    "description", "Target completion date in YYYY-MM-DD format (e.g., 2024-12-31). Optional."
)
```

### 3. Error Handling

```java
@Override
public String execute(String input) {
    try {
        // Tool logic here
        return "Success: " + result;
    } catch (JsonProcessingException e) {
        return "Error: Invalid JSON format. " + e.getMessage();
    } catch (IllegalArgumentException e) {
        return "Error: Invalid parameter value. " + e.getMessage();
    } catch (Exception e) {
        return "Error: Unexpected error occurred. " + e.getMessage();
    }
}
```

### 4. Rich Response Format

```java
StringBuilder result = new StringBuilder();
result.append("✅ Goal created successfully!\n");
result.append("ID: ").append(goalId).append("\n");
result.append("Title: ").append(title).append("\n");
result.append("Category: ").append(category).append("\n");
result.append("Priority: ").append(priority).append("\n");
result.append("Created: ").append(LocalDateTime.now()).append("\n");

if (deadline != null) {
    result.append("Deadline: ").append(deadline).append("\n");
}

return result.toString();
```

## LangChain4j Integration Flow

1. **Tool Registration**: Framework registers tools with their parameter specifications
2. **LLM Understanding**: LangChain4j sends parameter schemas to the LLM
3. **Structured Calls**: LLM generates properly formatted tool calls with JSON parameters
4. **Parameter Extraction**: Framework extracts parameters from tool call
5. **Tool Execution**: Tool receives JSON string and parses parameters
6. **Result Processing**: Tool returns formatted result string

## Example Tool Call

### Input from LLM:

```json
{
  "id": "call_12345",
  "name": "addGoal",
  "arguments": {
    "title": "Learn Python",
    "category": "education",
    "priority": "high",
    "deadline": "2024-06-30"
  }
}
```

### Tool Execution:

```java
execute('{"title":"Learn Python","category":"education","priority":"high","deadline":"2024-06-30"}')
```

### Tool Response:

```
✅ Goal created successfully!
ID: goal_1703123456789
Title: Learn Python
Category: education
Priority: high
Deadline: 2024-06-30
Created: 2023-12-21T10:30:45
```

This structured approach ensures that your tools work seamlessly with LangChain4j's advanced tool calling capabilities while providing clear parameter validation and error handling.
