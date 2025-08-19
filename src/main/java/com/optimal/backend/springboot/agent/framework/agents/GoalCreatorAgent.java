package com.optimal.backend.springboot.agent.framework.agents;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.optimal.backend.springboot.agent.framework.agents.prompts.GoalCreatorPrompt;
import com.optimal.backend.springboot.agent.framework.core.BaseAgent;
import com.optimal.backend.springboot.agent.framework.core.LlmClient;
import com.optimal.backend.springboot.agent.framework.tools.CreateGoalTool;
import com.optimal.backend.springboot.agent.framework.tools.GetGoalDescriptionTool;
import com.optimal.backend.springboot.agent.framework.tools.GetFutureDateTool;

import jakarta.annotation.PostConstruct;

@Component
public class GoalCreatorAgent extends BaseAgent {
    private final CreateGoalTool createGoalTool;
    private final GetGoalDescriptionTool goalDescriptionTool;
    private final GetFutureDateTool getFutureDateTool;

    @Autowired
    public GoalCreatorAgent(
            @Value("${langchain4j.goal-creator-agent.name}") String name,
            @Value("${langchain4j.goal-creator-agent.description}") String description,
            CreateGoalTool createGoalTool,
            GetGoalDescriptionTool goalDescriptionTool,
            GetFutureDateTool getFutureDateTool,
            LlmClient llmClient) {
        super(name, description, GoalCreatorPrompt.getDefaultPrompt(), llmClient);
        this.createGoalTool = createGoalTool;
        this.goalDescriptionTool = goalDescriptionTool;
        this.getFutureDateTool = getFutureDateTool;

        addTool(createGoalTool);
        addTool(goalDescriptionTool);
        addTool(getFutureDateTool);
    }

    @PostConstruct
    @Override
    protected void initialize() {
        System.out.println("GoalCreatorAgent initialized with tools: " + getTools().size());
        getTools().forEach(tool -> System.out.println("- " + tool.getName() + ": " + tool.getDescription()));
    }
}
