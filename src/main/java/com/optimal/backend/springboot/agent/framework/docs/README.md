# Agent Framework with LangChain4j Integration

This package provides a robust agent framework that integrates with LangChain4j for LLM interactions and tool calling capabilities.

## Architecture

### Core Components

1. **BaseAgent** - Abstract base class for all agents
2. **LlmClient** - Handles LLM interactions via LangChain4j
3. **Tool Interface** - Contract for agent tools
4. **Message** - Wrapper for chat messages with LangChain4j compatibility
5. **LlmResponse** - Response wrapper with tool call extraction

### Key Features

- ✅ **Tool Call Detection & Execution** - Automatically detects and executes tool calls from LLM responses
- ✅ **LangChain4j Integration** - Full compatibility with LangChain4j's ChatLanguageModel
- ✅ **Spring Boot Configuration** - Easy configuration via application properties
- ✅ **Fallback Mechanism** - Graceful degradation when LLM is unavailable
- ✅ **Type-Safe Tool Interface** - Structured tool development

## Configuration

### 1. Environment Variables

```bash
export OPENAI_API_KEY="your-openai-api-key"
```

### 2. Application Properties

```properties
# LangChain4j Configuration
langchain4j.open-ai.chat-model.api-key=${OPENAI_API_KEY:demo}
langchain4j.open-ai.chat-model.model-name=gpt-3.5-turbo
langchain4j.open-ai.chat-model.temperature=0.7
langchain4j.open-ai.chat-model.max-tokens=1000
```

## Usage

### Creating a Custom Agent

```java
@Component
public class GoalAgent extends BaseAgent {

    @Autowired
    private AddGoalTool addGoalTool;

    @PostConstruct
    public void init() {
        setName("GoalAgent");
        setDescription("Manages user goals by creating, updating, or removing them");
        setSystemPrompt("You help users manage their goals. You can add new goals, update progress, or delete them. Use tools to perform these actions.");

        // Add tools
        addTool(addGoalTool);
    }
}
```

### Creating a Custom Tool

```java
@Component
public class AddGoalTool implements Tool {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String getName() {
        return "addGoal";
    }

    @Override
    public String execute(String input) {
        try {
            // Parse JSON input to extract parameters
            JsonNode inputNode = objectMapper.readTree(input);

            String title = inputNode.get("title").asText();
            String category = inputNode.has("category") ? inputNode.get("category").asText() : "personal";

            // Add goal to database
            return "✅ Goal added successfully: " + title + " (Category: " + category + ")";

        } catch (Exception e) {
            return "Error adding goal: " + e.getMessage();
        }
    }

    @Override
    public String getDescription() {
        return "Add a new goal to the user's goal list. Requires a title and optionally accepts category, priority, and deadline.";
    }

    @Override
    public ToolParameters getParameters() {
        return ToolParameters.builder()
                .type("object")
                .properties(Map.of(
                    "title", Map.of("type", "string", "description", "The title/name of the goal (required)"),
                    "category", Map.of("type", "string", "description", "Category of the goal",
                                     "enum", Arrays.asList("personal", "work", "health", "education")),
                    "priority", Map.of("type", "string", "description", "Priority level",
                                     "enum", Arrays.asList("low", "medium", "high", "critical"))
                ))
                .required(Arrays.asList("title"))
                .build();
    }
}
```

### Running an Agent

```java
@RestController
public class AgentController {

    @Autowired
    private GoalAgent goalAgent;

    @PostMapping("/agent/goal")
    public List<Message> processGoalRequest(@RequestBody String userInput) {
        List<Message> instructions = Arrays.asList(
            new Message("user", userInput)
        );

        return goalAgent.run(instructions);
    }
}
```

## Tool Parameters

Tools can define structured parameters using the `getParameters()` method. This enables the LLM to understand exactly what parameters are required and their types.

### Parameter Types

- **String**: `Map.of("type", "string", "description", "...")`
- **Number**: `Map.of("type", "number", "description", "...")`
- **Boolean**: `Map.of("type", "boolean", "description", "...")`
- **Enum**: `Map.of("type", "string", "enum", Arrays.asList("option1", "option2"), "description", "...")`

### Required vs Optional Parameters

```java
.required(Arrays.asList("title", "category")) // Required parameters
// All other parameters are optional
```

## Tool Call Flow

1. **User Input** → Agent receives instructions
2. **LLM Generation** → LangChain4j generates response with structured tool calls
3. **Tool Detection** → Framework detects tool calls in response
4. **Parameter Extraction** → JSON parameters are extracted from tool calls
5. **Tool Execution** → Matching tools are executed with structured inputs
6. **Context Update** → Tool results are added back to conversation context
7. **Iteration** → Process repeats until no more tool calls (max 20 steps)

## Error Handling

- **LLM API Failures** → Graceful fallback to simulated responses
- **Missing Tools** → Logs warning and continues execution
- **Tool Execution Errors** → Error message added to context
- **Invalid Configurations** → Uses mock ChatLanguageModel for development

## Development vs Production

### Development Mode

- Uses mock responses when `api-key=demo`
- Provides structured feedback for testing
- No external API calls required

### Production Mode

- Requires valid OpenAI API key
- Full LangChain4j integration
- Real-time LLM interactions

## Example Tool Call Sequence

```
User: "Add a goal to learn Spanish with high priority in the education category"
↓
LLM Response: {
  content: "I'll add that goal for you.",
  toolCalls: [{
    id: "call_123",
    name: "addGoal",
    input: {
      "title": "learn Spanish",
      "category": "education",
      "priority": "high"
    }
  }]
}
↓
Tool Execution: addGoal(JSON) → "✅ Goal added successfully: learn Spanish (Category: education, Priority: high)"
↓
Context Update: [
  {role: "user", content: "Add a goal to learn Spanish with high priority in the education category"},
  {role: "assistant", content: "I'll add that goal for you."},
  {role: "tool", content: "✅ Goal added successfully: learn Spanish (Category: education, Priority: high)", toolExecutionId: "call_123"}
]
↓
Final LLM Response: "Perfect! I've successfully added 'learn Spanish' to your education goals with high priority."
```

## Dependencies

```xml
<dependency>
    <groupId>dev.langchain4j</groupId>
    <artifactId>langchain4j-spring-boot-starter</artifactId>
    <version>0.34.0</version>
</dependency>
```
