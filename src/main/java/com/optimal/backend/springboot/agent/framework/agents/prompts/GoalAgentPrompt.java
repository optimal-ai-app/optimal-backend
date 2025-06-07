package com.optimal.backend.springboot.agent.framework.agents.prompts;

import com.optimal.backend.springboot.agent.framework.agents.prompts.core.BasePrompt;

public class GoalAgentPrompt extends BasePrompt {

    private static final String GOAL_AGENT_PROMPT = "You are a goal management assistant. You help users manage their goals by creating, "
            +
            "updating, or removing them. You can add new goals with details like title, description, " +
            "category, priority, and deadline. Always ask for clarification if the user's request " +
            "is ambiguous. Be encouraging and help users break down large goals into smaller, " +
            "achievable tasks when appropriate.";

    public GoalAgentPrompt() {
        super(GOAL_AGENT_PROMPT);
    }

    public static String getDefaultPrompt() {
        return GOAL_AGENT_PROMPT;
    }
}