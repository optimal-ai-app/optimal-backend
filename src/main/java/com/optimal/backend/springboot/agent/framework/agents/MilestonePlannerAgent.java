package com.optimal.backend.springboot.agent.framework.agents;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.optimal.backend.springboot.agent.framework.agents.prompts.MilestonePlannerPrompt;
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
public class MilestonePlannerAgent extends BaseAgent {

    private GetGoalDescriptionTool goalDescriptionTool;
    private GetTasksforGoalTool getTasksforGoalTool;
    private GetGoalProgressTool getGoalProgressTool;
    private GetGoalMilestoneTool getGoalMilestoneTool;
    private GetFutureDateTool getFutureDateTool;

    @Autowired
    public MilestonePlannerAgent(
            LlmClient llmClient,
            GetGoalDescriptionTool goalDescriptionTool,
            GetTasksforGoalTool getTasksforGoalTool,
            GetGoalProgressTool getGoalProgressTool,
            GetGoalMilestoneTool getGoalMilestoneTool,
            GetFutureDateTool getFutureDateTool) {

        super("MilestonePlannerAgent",
                "Helps the user plan milestones for their qualitative goals",
                MilestonePlannerPrompt.getDefaultPrompt(),
                llmClient);

        this.goalDescriptionTool = goalDescriptionTool;
        this.getTasksforGoalTool = getTasksforGoalTool;
        this.getGoalProgressTool = getGoalProgressTool;
        this.getGoalMilestoneTool = getGoalMilestoneTool;
        this.getFutureDateTool = getFutureDateTool;

        addTool(this.goalDescriptionTool);
        addTool(this.getTasksforGoalTool);
        addTool(this.getGoalProgressTool);
        addTool(this.getGoalMilestoneTool);
        addTool(this.getFutureDateTool);
    }

    @PostConstruct
    @Override
    protected void initialize() {
        System.out.println("MilestonePlannerAgent initialized with tools: " + getTools().size());
        getTools().forEach(tool -> System.out.println("- " + tool.getClass().getSimpleName()));
    }
}