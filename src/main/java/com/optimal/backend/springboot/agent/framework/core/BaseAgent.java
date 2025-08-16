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
 * Updated to work with Langchain4j @Tool annotated classes.
 * 
 * This class is Spring-managed and supports dependency injection.
 */
public abstract class BaseAgent {
    private static final int MAX_STEPS = 20;

    protected String name;
    protected String description;
    protected String systemPrompt;
    protected List<Object> tools; // Changed to Object to accept @Tool annotated classes
    protected LlmClient llmClient;

    public BaseAgent() {
        this.tools = new ArrayList<>();
    }

    public BaseAgent(String name, String description, String systemPrompt, List<Object> tools, LlmClient llmClient) {
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
                Message assistantMessage = new Message(response.getAiMessage());
                contexts.add(assistantMessage);
                System.out.println("Added assistant message with tool calls to context");

                // Tool calls are automatically executed by Langchain4j when using @Tool
                // annotations
                // The tool responses should be included in the conversation automatically
                System.out.println("Tool execution handled automatically by Langchain4j");
            } else {
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

    private String getResponseContent(LlmResponse response) {
        String content = response.getContent();
        if (content == null || content.trim().isEmpty()) {
            return response.hasToolCalls() ? "" : "No response provided";
        }
        return content;
    }

    // Remove private helper methods that are no longer needed

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