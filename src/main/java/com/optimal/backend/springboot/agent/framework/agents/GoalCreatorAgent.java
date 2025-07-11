package com.optimal.backend.springboot.agent.framework.agents;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.optimal.backend.springboot.agent.framework.agents.prompts.GoalCreatorPrompt;
import com.optimal.backend.springboot.agent.framework.core.BaseAgent;
import com.optimal.backend.springboot.agent.framework.core.LlmClient;
import com.optimal.backend.springboot.agent.framework.tools.GoalAgentCreateGoalTool;
import com.optimal.backend.springboot.agent.framework.tools.GoalDescriptionTool;

import jakarta.annotation.PostConstruct;

@Component
public class GoalCreatorAgent extends BaseAgent {
    private final GoalAgentCreateGoalTool createGoalTool;
    private final GoalDescriptionTool goalDescriptionTool;

    @Autowired
    public GoalCreatorAgent(
            @Value("${langchain4j.goal-creator-agent.name}") String name,
            @Value("${langchain4j.goal-creator-agent.description}") String description,
            GoalAgentCreateGoalTool createGoalTool,
            GoalDescriptionTool goalDescriptionTool,
            LlmClient llmClient) {
        super(name, description, GoalCreatorPrompt.getDefaultPrompt(), llmClient);
        this.createGoalTool = createGoalTool;
        this.goalDescriptionTool = goalDescriptionTool;

        addTool(createGoalTool);
        addTool(goalDescriptionTool);
    }

    @PostConstruct
    @Override
    protected void initialize() {
        System.out.println("GoalCreatorAgent initialized with tools: " + getTools().size());
        getTools().forEach(tool -> System.out.println("- " + tool.getName() + ": " + tool.getDescription()));
    }
}
