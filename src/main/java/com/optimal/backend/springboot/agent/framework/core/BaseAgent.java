package com.optimal.backend.springboot.agent.framework.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.optimal.backend.springboot.agent.framework.core.system.GeneralPromptAppender;

import jakarta.annotation.PostConstruct;

/**
 * Base class for all agents in the framework.
 * Each agent is defined by its name, description, system prompt, available
 * tools, and run method.
 * 
 * This class is Spring-managed and supports dependency injection.
 */
public abstract class BaseAgent {
    private static final int MAX_STEPS = 20;

    protected String name;
    protected String description;
    protected String systemPrompt;
    protected List<Tool> tools;

    protected LlmClient llmClient;

    public BaseAgent() {
        this.tools = new ArrayList<>();
    }

    public BaseAgent(String name, String description, String systemPrompt, List<Tool> tools, LlmClient llmClient) {
        this.name = name;
        this.description = description;
        this.systemPrompt = GeneralPromptAppender.appendGeneralInstructions(systemPrompt);
        this.tools = tools != null ? tools : new ArrayList<>();
        this.llmClient = llmClient;
    }

    public BaseAgent(String name, String description, String systemPrompt, LlmClient llmClient) {
        this(name, description, systemPrompt, new ArrayList<>(), llmClient);
    }

    /**
     * Initialization method called after Spring dependency injection.
     * Override this method in subclasses to configure the agent.
     */
    @PostConstruct
    protected void initialize() {
        // Override in subclasses to configure the agent
    }

    public void addTool(Tool tool) {
        if (tool != null) {
            this.tools.add(tool);
        }
    }

    public void removeTool(Tool tool) {
        this.tools.remove(tool);
    }

    public List<Message> run(List<Message> instructions) {
        List<Message> contexts = new ArrayList<>(instructions);
        Map<String, Tool> toolMap = createToolMap();

        System.out.println("=== Agent Run Started ===");
        System.out.println("Initial contexts: " + contexts.size());

        for (int step = 0; step < MAX_STEPS; step++) {
            System.out.println("\n=== STEP " + (step + 1) + " ===");
            System.out.println("Sending to LLM - Contexts: " + contexts.size());

            LlmResponse response = llmClient.generate(systemPrompt, contexts, tools);
            String responseContent = getResponseContent(response);

            System.out.println("LLM Response: " + responseContent);
            System.out.println("Has tool calls: " + response.hasToolCalls());

            if (response.hasToolCalls()) {
                // For responses with tool calls, we need to preserve the original AiMessage
                // to maintain the tool call metadata that OpenAI requires
                System.out.println("Processing all " + response.getToolCalls().size() + " tool calls");

                // Create assistant message from the original AiMessage to preserve tool calls
                Message assistantMessage = new Message(response.getAiMessage());
                contexts.add(assistantMessage);
                System.out.println("Added assistant message with tool calls to context");

                // Process ALL tool calls to satisfy OpenAI's requirement
                // OpenAI requires that each tool call ID gets a corresponding tool response
                processToolCalls(response.getToolCalls(), toolMap, contexts);
                System.out.println("After tool calls - Contexts: " + contexts.size());
            } else {
                // For responses without tool calls, add the text content as usual
                if (!responseContent.trim().isEmpty()) {
                    Message assistantMessage = new Message("assistant", responseContent, responseContent);
                    contexts.add(assistantMessage);
                    System.out.println("Added assistant message to context");
                }
                System.out.println("No tool calls, ending conversation");
                break;
            }
        }

        System.out.println("=== Agent Run Completed ===");
        return contexts;
    }

    private Map<String, Tool> createToolMap() {
        Map<String, Tool> toolMap = new HashMap<>();
        for (Tool tool : tools) {
            toolMap.put(tool.getName(), tool);
        }
        return toolMap;
    }

    private String getResponseContent(LlmResponse response) {
        String content = response.getContent();
        if (content == null || content.trim().isEmpty()) {
            return response.hasToolCalls() ? "" : "No response provided";
        }
        return content;
    }

    private void processToolCalls(List<ToolCall> toolCalls, Map<String, Tool> toolMap, List<Message> contexts) {
        for (ToolCall toolCall : toolCalls) {
            System.out.println("Executing tool: " + toolCall.getName() + " with ID: " + toolCall.getId());
            System.out.println("Tool input: " + toolCall.getInput());

            Tool tool = toolMap.get(toolCall.getName());
            Message toolMessage = executeTool(tool, toolCall);

            System.out.println("Tool execution result:");
            System.out.println("- Role: " + toolMessage.getRole());
            System.out.println("- Content: " + toolMessage.getContent());
            System.out.println("- ToolExecutionId: " + toolMessage.getToolExecutionId());

            contexts.add(toolMessage);
        }
    }

    private Message executeTool(Tool tool, ToolCall toolCall) {
        String output;

        if (tool != null) {
            try {
                output = tool.execute(toolCall.getInput());
            } catch (Exception e) {
                output = "Error executing tool: " + e.getMessage();
            }
        } else {
            output = "Error: Tool '" + toolCall.getName() + "' not found";
        }

        Message message = new Message("tool", output, output);
        message.setToolExecutionId(toolCall.getId());
        return message;
    }

    // Getters and setters
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

    public String getSystemPrompt() {
        return systemPrompt;
    }

    public void setSystemPrompt(String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }

    public List<Tool> getTools() {
        return tools;
    }

    public void setTools(List<Tool> tools) {
        this.tools = tools != null ? tools : new ArrayList<>();
    }

    @Override
    public String toString() {
        return String.format("Agent{name='%s', description='%s', tools=%d}",
                name, description, tools.size());
    }
}
