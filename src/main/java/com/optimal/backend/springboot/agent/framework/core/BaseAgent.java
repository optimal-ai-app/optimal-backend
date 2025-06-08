package com.optimal.backend.springboot.agent.framework.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private static final int MAX_STEPS = 20;
    
    protected String name;
    protected String description;
    protected String systemPrompt;
    protected List<Tool> tools;
    
    @Autowired
    protected LlmClient llmClient;

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
        this(name, description, systemPrompt, new ArrayList<>());
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

        for (int step = 0; step < MAX_STEPS; step++) {
            LlmResponse response = llmClient.generate(systemPrompt, contexts, tools);
            
            String responseContent = getResponseContent(response);
            if (!responseContent.trim().isEmpty()) {
                contexts.add(new Message("assistant", responseContent, responseContent));
            }

            if (response.hasToolCalls()) {
                processToolCalls(response.getToolCalls(), toolMap, contexts);
            } else {
                break;
            }
        }
        
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
            Tool tool = toolMap.get(toolCall.getName());
            Message toolMessage = executeTool(tool, toolCall);
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
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getSystemPrompt() { return systemPrompt; }
    public void setSystemPrompt(String systemPrompt) { this.systemPrompt = systemPrompt; }
    
    public List<Tool> getTools() { return tools; }
    public void setTools(List<Tool> tools) { this.tools = tools != null ? tools : new ArrayList<>(); }

    @Override
    public String toString() {
        return String.format("Agent{name='%s', description='%s', tools=%d}", 
                name, description, tools.size());
    }
}
