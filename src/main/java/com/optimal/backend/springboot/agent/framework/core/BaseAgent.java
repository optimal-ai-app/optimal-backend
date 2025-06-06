package com.optimal.backend.springboot.agent.framework.core;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;

/**
 * Base class for all agents in the framework.
 * Each agent is defined by its name, description, system prompt, available
 * tools, and run method.
 * 
 * This class is Spring-managed and supports dependency injection.
 */
@Component
public abstract class BaseAgent {
    final protected int MAX_STEPS = 20;
    protected String name;
    protected String description;
    protected String systemPrompt;
    protected List<Object> tools;

    @Autowired
    protected LlmClient llmClient;

    public BaseAgent() {
        this.tools = new ArrayList<>();
    }

    public BaseAgent(String name, String description, String systemPrompt, List<Object> tools) {
        this.name = name;
        this.description = description;
        this.systemPrompt = systemPrompt;
        this.tools = tools != null ? tools : new ArrayList<>();
    }

    public BaseAgent(String name, String description, String systemPrompt) {
        this(name, description, systemPrompt, new ArrayList<>());
    }

    /**
     * Initialization method called after Spring dependency injection.
     * Override this method in subclasses to configure the agent.
     */
    @PostConstruct
    protected void initialize() {
        // Subclasses should override this method to configure the agent
        // Example:
        // setName("MyAgent");
        // setSystemPrompt("You are a helpful assistant...");
        // addTool(myTool);
    }

    public void addTool(Object tool) {
        if (tool != null) {
            this.tools.add(tool);
        }
    }

    public void removeTool(Object tool) {
        this.tools.remove(tool);
    }

    public List<Message> run(List<Message> instructions) {
        List<Message> contexts = new ArrayList<>(instructions);

        // Create tool map for quick lookup by name
        Map<String, Object> toolMap = new HashMap<>();
        for (Object tool : tools) {
            if (tool instanceof Tool) {
                Tool toolInstance = (Tool) tool;
                toolMap.put(toolInstance.getName(), toolInstance);
            }
        }

        for (int i = 0; i < MAX_STEPS; i++) {
            LlmResponse response = llmClient.generate(systemPrompt, contexts, tools);

            // Check if response has tool calls
            if (response.hasToolCalls()) {
                // Process each tool call
                for (ToolCall toolCall : response.getToolCalls()) {
                    // Execute the tool call
                    Object tool = toolMap.get(toolCall.getName());
                    if (tool instanceof Tool) {
                        Tool toolInstance = (Tool) tool;
                        try {
                            String output = toolInstance.execute(toolCall.getInput());

                            // Add tool response to context
                            Message toolMessage = new Message();
                            toolMessage.setRole("tool");
                            toolMessage.setToolExecutionId(toolCall.getId());
                            toolMessage.setContent(output);
                            contexts.add(toolMessage);

                        } catch (Exception e) {
                            // Handle tool execution errors gracefully
                            Message errorMessage = new Message();
                            errorMessage.setRole("tool");
                            errorMessage.setToolExecutionId(toolCall.getId());
                            errorMessage.setContent("Error executing tool: " + e.getMessage());
                            contexts.add(errorMessage);
                        }
                    } else {
                        // Tool not found - add error message
                        Message errorMessage = new Message();
                        errorMessage.setRole("tool");
                        errorMessage.setToolExecutionId(toolCall.getId());
                        errorMessage.setContent("Error: Tool '" + toolCall.getName() + "' not found");
                        contexts.add(errorMessage);
                    }
                }
            } else {
                // No more tool calls, we can break the loop
                break;
            }
        }
        return contexts;
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

    public List<Object> getTools() {
        return tools;
    }

    public void setTools(List<Object> tools) {
        this.tools = tools != null ? tools : new ArrayList<>();
    }

    @Override
    public String toString() {
        return String.format("Agent{name='%s', description='%s', tools=%d}",
                name, description, tools.size());
    }
}
