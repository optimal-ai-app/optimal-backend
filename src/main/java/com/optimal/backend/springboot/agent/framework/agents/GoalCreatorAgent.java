package com.optimal.backend.springboot.agent.framework.agents;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.optimal.backend.springboot.agent.framework.agents.prompts.GoalCreatorPrompt;
import com.optimal.backend.springboot.agent.framework.core.BaseAgent;
import com.optimal.backend.springboot.agent.framework.core.LlmClient;
import com.optimal.backend.springboot.agent.framework.tools.GetFutureDateTool;

import jakarta.annotation.PostConstruct;

@Component
public class GoalCreatorAgent extends BaseAgent {
    @SuppressWarnings("unused")
    private final GetFutureDateTool getFutureDateTool;

    @Autowired
    public GoalCreatorAgent(
            @Value("${langchain4j.goal-creator-agent.name}") String name,
            @Value("${langchain4j.goal-creator-agent.description}") String description,
            GetFutureDateTool getFutureDateTool,
            LlmClient llmClient) {
        super(name, description, GoalCreatorPrompt.getDefaultPrompt(), llmClient);
        this.getFutureDateTool = getFutureDateTool;
        addTool(getFutureDateTool);
    }

    @PostConstruct
    @Override
    protected void initialize() {
        System.out.println("GoalCreatorAgent initialized with tools: " + getTools().size());
        getTools().forEach(tool -> System.out.println("- " + tool.getClass().getSimpleName()));
    }
}
