package com.optimal.backend.springboot.agent.framework.core;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.optimal.backend.springboot.agent.framework.core.system.GeneralPromptAppender;

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
    final protected int MAX_STEPS = 20; // Maximum number of steps an agent can take
    protected String name; // Name of the agent
    protected String description; // Description of the agent
    protected String systemPrompt; // System prompt for the agent
    protected List<Tool> tools; // List of tools available to the agent

     // This just means that spring will inject the LlmClient bean into the field
    // We do not need to instantiate it here, spring will do it for us
    @Autowired
    protected LlmClient llmClient;

    // Constructors
    public BaseAgent() {
        this.tools = new ArrayList<>();
    }

    public BaseAgent(String name, String description, String systemPrompt, List<Tool> tools) {
        this.name = name;
        this.description = description;
        this.systemPrompt = GeneralPromptAppender.appendGeneralInstructions(systemPrompt);
        this.tools = tools != null ? tools : new ArrayList<>();
    }

    public BaseAgent(String name, String description, String systemPrompt) {
        this(name, description, GeneralPromptAppender.appendGeneralInstructions(systemPrompt), new ArrayList<>());
    }
    //

    /**
     * Initialization method called after Spring dependency injection.
     * Override this method in subclasses to configure the agent.
     */
    // We may not need this, but it's a good practice to have it
    @PostConstruct
    protected void initialize() {
        // Subclasses should override this method to configure the agent
        // Example:
        // setName("MyAgent");
        // setSystemPrompt("You are a helpful assistant...");
        // addTool(myTool);
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

        // Create tool map for quick lookup by name
        Map<String, Tool> toolMap = new HashMap<>();
        for (Tool tool : tools) {
            if (tool instanceof Tool) {
                Tool toolInstance = (Tool) tool;
                toolMap.put(toolInstance.getName(), toolInstance);
            }
        }

        for (int i = 0; i < this.MAX_STEPS; i++) {
            LlmResponse response = llmClient.generate(systemPrompt, contexts, tools);
            // Add AI response to context
            Message aiMessage = new Message();
            aiMessage.setRole("assistant");
            aiMessage.setContent(response.getContent());
            aiMessage.setMessage(response.getContent());
            System.out.println(this.name + " response: " + response.getContent());

            contexts.add(aiMessage);

            // Check if response has tool calls
            if (response.hasToolCalls()) {
                // Process each tool call
                for (ToolCall toolCall : response.getToolCalls()) {
                    // Execute the tool call
                    Tool tool = toolMap.get(toolCall.getName());
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
