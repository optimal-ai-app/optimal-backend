package com.optimal.backend.springboot.agent.framework.agents;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.optimal.backend.springboot.agent.framework.agents.prompts.TaskCreatorPrompt;
import com.optimal.backend.springboot.agent.framework.core.BaseAgent;
import com.optimal.backend.springboot.agent.framework.core.LlmClient;
import com.optimal.backend.springboot.agent.framework.tools.GetFutureDateTool;

import jakarta.annotation.PostConstruct;

/**
 * TaskCreatorAgent handles creation of regular (non-milestone) tasks.
 * 
 * This agent:
 * - Receives task planning data from TaskPlannerAgent
 * - Creates regular tasks that contribute to milestones
 * - Depends on TaskPlannerAgent for task planning data
 */
@Component
@Scope("prototype")
public class TaskCreatorAgent extends BaseAgent {
    private GetFutureDateTool getFutureDateTool;

    @Autowired
    public TaskCreatorAgent(
            LlmClient llmClient, GetFutureDateTool getFutureDateTool) {
        super("TaskCreatorAgent",
                "Creates regular (non-milestone) tasks based on task planning data from TaskPlannerAgent.",
                TaskCreatorPrompt.getDefaultPrompt(), llmClient);
        this.getFutureDateTool = getFutureDateTool;
        addTool(this.getFutureDateTool);
    }

    @PostConstruct
    @Override
    protected void initialize() {
        System.out.println("TaskCreatorAgent initialized with tools: " + getTools().size());
        getTools().forEach(tool -> System.out.println("- " + tool.getClass().getSimpleName()));
    }
}
