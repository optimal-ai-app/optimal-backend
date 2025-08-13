package com.optimal.backend.springboot.agent.framework.agents;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.optimal.backend.springboot.agent.framework.agents.prompts.TaskCreatorPrompt;
import com.optimal.backend.springboot.agent.framework.core.BaseAgent;
import com.optimal.backend.springboot.agent.framework.core.LlmClient;
import com.optimal.backend.springboot.agent.framework.tools.CreateTaskTool;

import jakarta.annotation.PostConstruct;

@Component
public class TaskCreatorAgent extends BaseAgent {
    private final CreateTaskTool createTaskTool;

    @Autowired
    public TaskCreatorAgent(
            @Value("${langchain4j.task-creator-agent.name}") String name,
            @Value("${langchain4j.task-creator-agent.description}") String description,
            CreateTaskTool createTaskTool,
            LlmClient llmClient) {
        super(name, description, TaskCreatorPrompt.getDefaultPrompt(), llmClient);
        this.createTaskTool = createTaskTool;

        addTool(createTaskTool);
    }

    @PostConstruct
    @Override
    protected void initialize() {
        System.out.println("TaskCreatorAgent initialized with tools: " + getTools().size());
        getTools().forEach(tool -> System.out.println("- " + tool.getName() + ": " + tool.getDescription()));
    }
}
