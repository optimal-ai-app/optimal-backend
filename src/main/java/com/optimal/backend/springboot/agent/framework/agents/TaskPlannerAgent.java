package com.optimal.backend.springboot.agent.framework.agents;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.optimal.backend.springboot.agent.framework.agents.prompts.TaskPlannerPrompt;
import com.optimal.backend.springboot.agent.framework.core.BaseAgent;
import com.optimal.backend.springboot.agent.framework.core.LlmClient;
import com.optimal.backend.springboot.agent.framework.tools.GetGoalDescriptionTool;
import com.optimal.backend.springboot.agent.framework.tools.GetGoalProgressTool;
import com.optimal.backend.springboot.agent.framework.tools.GetGoalMilestoneTool;
import com.optimal.backend.springboot.agent.framework.tools.GetTasksforGoalTool;
import com.optimal.backend.springboot.agent.framework.tools.GetFutureDateTool;

import jakarta.annotation.PostConstruct;

@Component
@Scope("prototype")
public class TaskPlannerAgent extends BaseAgent {

    private GetGoalDescriptionTool goalDescriptionTool;

    private GetTasksforGoalTool getTasksforGoalTool;

    private GetGoalProgressTool getGoalProgressTool;

    private GetGoalMilestoneTool getGoalMilestoneTool;

    private GetFutureDateTool getFutureDateTool;

    @Autowired
    public TaskPlannerAgent(
            LlmClient llmClient, GetGoalDescriptionTool getGoalDescriptionTool,
            GetTasksforGoalTool getTasksforGoalTool, GetGoalProgressTool getGoalProgressTool,
            GetGoalMilestoneTool getGoalMilestoneTool, GetFutureDateTool getFutureDateTool) {
        super("TaskPlannerAgent",
                "Independently plans and suggests tasks based on any input, whether from goals or direct user requests. Works autonomously to break down objectives into actionable tasks.",
                TaskPlannerPrompt.getDefaultPrompt(), llmClient);

        this.goalDescriptionTool = getGoalDescriptionTool;
        this.getTasksforGoalTool = getTasksforGoalTool;
        this.getGoalMilestoneTool = getGoalMilestoneTool;
        this.getGoalProgressTool = getGoalProgressTool;
        this.getFutureDateTool = getFutureDateTool;

        addTool(this.goalDescriptionTool);
        addTool(this.getTasksforGoalTool);
        addTool(this.getGoalMilestoneTool);
        addTool(this.getGoalProgressTool);
        addTool(this.getFutureDateTool);
    }

    @PostConstruct
    @Override
    protected void initialize() {

        System.out.println("TaskPlannerAgent initialized with CHOOLSE: " + getTools().size());
        getTools().forEach(tool -> System.out.println("- " + tool.getClass().getSimpleName()));
    }
}
