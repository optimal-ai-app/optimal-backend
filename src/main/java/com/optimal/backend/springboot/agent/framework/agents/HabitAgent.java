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
    private final CreateHabitTool createHabitTool = new CreateHabitTool();

    @Autowired
    public HabitAgent(
            LlmClient llmClient) {
        super("HabitAgent", "Creates and manages habits, seeds actions, and logs completions",
                HabitAgentPrompt.getDefaultPrompt(), llmClient);
        addTool(createHabitTool);
    }

    @PostConstruct
    @Override
    protected void initialize() {
        System.out.println("HabitAgent initialized with tools: " + getTools().size());
        getTools().forEach(tool -> System.out.println("- " + tool.getClass().getSimpleName()));
    }
}
