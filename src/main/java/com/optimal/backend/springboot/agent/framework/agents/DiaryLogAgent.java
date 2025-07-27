package com.optimal.backend.springboot.agent.framework.agents;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.optimal.backend.springboot.agent.framework.agents.prompts.DiaryLogAgentPrompt;
import com.optimal.backend.springboot.agent.framework.core.BaseAgent;
import com.optimal.backend.springboot.agent.framework.core.LlmClient;
import com.optimal.backend.springboot.agent.framework.tools.GetTasksforGoalTool;
import com.optimal.backend.springboot.agent.framework.tools.GoalDescriptionTool;

import jakarta.annotation.PostConstruct;

@Component
public class DiaryLogAgent extends BaseAgent {
    private final GoalDescriptionTool goalDescriptionTool;
    private final GetTasksforGoalTool getTasksforGoalTool;

    @Autowired
    public DiaryLogAgent(
            @Value("${langchain4j.diary-log-agent.name}") String name,
            @Value("${langchain4j.diary-log-agent.description}") String description,
            GoalDescriptionTool goalDescriptionTool, GetTasksforGoalTool getTasksforGoalTool,
            LlmClient llmClient) {
        super(name, description, DiaryLogAgentPrompt.getDefaultPrompt(), llmClient);
        this.goalDescriptionTool = goalDescriptionTool;
        this.getTasksforGoalTool = getTasksforGoalTool;
        addTool(goalDescriptionTool);
        addTool(getTasksforGoalTool);
    }

    @PostConstruct
    @Override
    protected void initialize() {
        System.out.println("DiaryLogAgent initialized with tools: " + getTools().size());
        getTools().forEach(tool -> System.out.println("- " + tool.getName() + ": " + tool.getDescription()));
    }
}
