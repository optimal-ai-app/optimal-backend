package com.optimal.backend.springboot.agent.framework.agents;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.optimal.backend.springboot.agent.framework.agents.prompts.TaskPlannerPrompt;
import com.optimal.backend.springboot.agent.framework.core.BaseAgent;
import com.optimal.backend.springboot.agent.framework.core.LlmClient;
import com.optimal.backend.springboot.agent.framework.tools.GetGoalDescriptionTool;
import com.optimal.backend.springboot.agent.framework.tools.GetGoalProgressTool;
import com.optimal.backend.springboot.agent.framework.tools.GetTasksforGoalTool;

import jakarta.annotation.PostConstruct;

@Component
public class TaskPlannerAgent extends BaseAgent {
    private final GetGoalDescriptionTool goalDescriptionTool;
    private final GetTasksforGoalTool getTasksforGoalTool;
    private final GetGoalProgressTool getGoalProgressTool;

    @Autowired
    public TaskPlannerAgent(
            @Value("${langchain4j.task-planner-agent.name}") String name,
            @Value("${langchain4j.task-planner-agent.description}") String description,
            GetGoalDescriptionTool goalDescriptionTool,
            GetTasksforGoalTool getTasksforGoalTool,
            GetGoalProgressTool getGoalProgressTool,
            LlmClient llmClient) {
        super(name, description, TaskPlannerPrompt.getDefaultPrompt(), llmClient);
        this.goalDescriptionTool = goalDescriptionTool;
        this.getTasksforGoalTool = getTasksforGoalTool;
        this.getGoalProgressTool = getGoalProgressTool;
        addTool(goalDescriptionTool);
        addTool(getTasksforGoalTool);
        addTool(getGoalProgressTool);
    }

    @PostConstruct
    @Override
    protected void initialize() {
        System.out.println("TaskPlannerAgent initialized with tools: " + getTools().size());
        getTools().forEach(tool -> System.out.println("- " + tool.getClass().getSimpleName()));
    }
}
