package com.optimal.backend.springboot.agent.framework.agents;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.optimal.backend.springboot.agent.framework.agents.prompts.GoalCreatorPrompt;
import com.optimal.backend.springboot.agent.framework.core.BaseAgent;
import com.optimal.backend.springboot.agent.framework.core.LlmClient;
import com.optimal.backend.springboot.agent.framework.tools.GetFutureDateTool;

import jakarta.annotation.PostConstruct;

@Component
@Scope("prototype")
public class GoalCreatorAgent extends BaseAgent {
    private GetFutureDateTool getFutureDateTool;

    @Autowired
    public GoalCreatorAgent(
            LlmClient llmClient, GetFutureDateTool getFutureDateTool) {
        super("GoalCreatorAgent",
                "Specializes exclusively in helping users define and articulate their high-level goals and aspirations. Does not handle task planning or creation.",
                GoalCreatorPrompt.getDefaultPrompt(), llmClient);
        this.getFutureDateTool = getFutureDateTool;
        addTool(this.getFutureDateTool);
    }

    @PostConstruct
    @Override
    protected void initialize() {
        System.out.println("GoalCreatorAgent initialized with tools: " + getTools().size());
        getTools().forEach(tool -> System.out.println("- " + tool.getClass().getSimpleName()));
    }
}
