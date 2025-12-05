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
import com.optimal.backend.springboot.agent.framework.tools.LlmMilestoneSuggestionTool;

import jakarta.annotation.PostConstruct;

@Component
@Scope("prototype")
public class MilestonePlannerAgent extends BaseAgent {

    private GetGoalDescriptionTool goalDescriptionTool;
    private GetTasksforGoalTool getTasksforGoalTool;
    private GetGoalProgressTool getGoalProgressTool;
    private GetGoalMilestoneTool getGoalMilestoneTool;
    private GetFutureDateTool getFutureDateTool;
    private LlmMilestoneSuggestionTool llmMilestoneSuggestionTool;

    @Autowired
    public MilestonePlannerAgent(
            LlmClient llmClient,
            GetGoalDescriptionTool goalDescriptionTool,
            GetTasksforGoalTool getTasksforGoalTool,
            GetGoalProgressTool getGoalProgressTool,
            GetGoalMilestoneTool getGoalMilestoneTool,
            GetFutureDateTool getFutureDateTool,
            LlmMilestoneSuggestionTool llmMilestoneSuggestionTool) {

        super("MilestonePlannerAgent",
                "Helps the user plan milestones for their qualitative goals",
                MilestonePlannerPrompt.getDefaultPrompt(),
                llmClient);

        this.goalDescriptionTool = goalDescriptionTool;
        this.getTasksforGoalTool = getTasksforGoalTool;
        this.getGoalProgressTool = getGoalProgressTool;
        this.getGoalMilestoneTool = getGoalMilestoneTool;
        this.getFutureDateTool = getFutureDateTool;
        this.llmMilestoneSuggestionTool = llmMilestoneSuggestionTool;

        addTool(this.goalDescriptionTool);
        addTool(this.getTasksforGoalTool);
        addTool(this.getGoalProgressTool);
        addTool(this.getGoalMilestoneTool);
        addTool(this.getFutureDateTool);
        addTool(this.llmMilestoneSuggestionTool);
    }

    @PostConstruct
    @Override
    protected void initialize() {
    }
}