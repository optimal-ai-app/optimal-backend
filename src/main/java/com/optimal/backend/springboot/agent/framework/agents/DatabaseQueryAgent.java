package com.optimal.backend.springboot.agent.framework.agents;

import com.optimal.backend.springboot.agent.framework.core.BaseAgent;
import com.optimal.backend.springboot.agent.framework.agents.prompts.DatabaseQueryAgentPrompt;
import com.optimal.backend.springboot.agent.framework.tools.BasicQueryTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * DatabaseQueryAgent - Performs database queries and provides user information
 * Demonstrates integration of database tools within the agent framework
 */
@Component("databaseQueryAgent")
public class DatabaseQueryAgent extends BaseAgent {

    @Autowired
    private BasicQueryTool basicQueryTool;

    @Override
    protected void initialize() {
        // Configure this agent
        setName("DatabaseQueryAgent");
        setDescription("Queries the database to retrieve the app's user information and nothing else.");
        setSystemPrompt(DatabaseQueryAgentPrompt.getDefaultPrompt());

        // Register the database query tool
        addTool(basicQueryTool);

        System.out.println("✅ DatabaseQueryAgent initialized with " + getTools().size() + " tools");
    }
}