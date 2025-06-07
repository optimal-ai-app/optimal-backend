package com.optimal.backend.springboot.agent.framework.agents;

import com.optimal.backend.springboot.agent.framework.core.BaseAgent;
import com.optimal.backend.springboot.agent.framework.agents.prompts.GoalAgentPrompt;
import com.optimal.backend.springboot.agent.framework.tools.AddGoalTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * GoalAgent - Manages user goals by creating, updating, or removing them
 * 
 * This agent demonstrates the simplicity of creating specialized agents
 * using the enhanced BaseAgent architecture.
 */
@Component("goalAgent")
public class GoalAgent extends BaseAgent {

    @Autowired
    private AddGoalTool addGoalTool;

    // Add more goal-related tools here as you develop them
    // @Autowired private UpdateGoalTool updateGoalTool;
    // @Autowired private DeleteGoalTool deleteGoalTool;
    // @Autowired private ListGoalsTool listGoalsTool;

    @Override
    protected void initialize() {
        // Configure this agent
        setName("GoalAgent");
        setDescription("Manages user goals by creating, updating, or removing them");
        setSystemPrompt(GoalAgentPrompt.getDefaultPrompt());

        // Register available tools
        addTool(addGoalTool);
        // addTool(updateGoalTool); // Add when implemented
        // addTool(deleteGoalTool); // Add when implemented
        // addTool(listGoalsTool); // Add when implemented

        System.out.println("✅ GoalAgent initialized with " + getTools().size() + " tools");
    }
}