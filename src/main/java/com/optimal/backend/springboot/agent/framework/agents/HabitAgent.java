package com.optimal.backend.springboot.agent.framework.agents;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.optimal.backend.springboot.agent.framework.agents.prompts.HabitAgentPrompt;
import com.optimal.backend.springboot.agent.framework.core.BaseAgent;
import com.optimal.backend.springboot.agent.framework.core.LlmClient;
import com.optimal.backend.springboot.agent.framework.tools.CreateHabitTool;

import jakarta.annotation.PostConstruct;

@Component
public class HabitAgent extends BaseAgent {
    private final CreateHabitTool createHabitTool;

    @Autowired
    public HabitAgent(
            @Value("${langchain4j.habit-agent.name:HabitAgent}") String name,
            @Value("${langchain4j.habit-agent.description:Creates and manages habits, seeds actions, and logs completions}") String description,
            CreateHabitTool createHabitTool,
            LlmClient llmClient) {
        super(name, description, HabitAgentPrompt.getDefaultPrompt(), llmClient);
        this.createHabitTool = createHabitTool;

        addTool(createHabitTool);
    }

    @PostConstruct
    @Override
    protected void initialize() {
        System.out.println("HabitAgent initialized with tools: " + getTools().size());
        getTools().forEach(tool -> System.out.println("- " + tool.getName() + ": " + tool.getDescription()));
    }
}


