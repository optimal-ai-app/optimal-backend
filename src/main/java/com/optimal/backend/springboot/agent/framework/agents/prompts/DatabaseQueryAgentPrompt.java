package com.optimal.backend.springboot.agent.framework.agents.prompts;

import com.optimal.backend.springboot.agent.framework.agents.prompts.core.BasePrompt;

public class DatabaseQueryAgentPrompt extends BasePrompt {

    private static final String DATABASE_QUERY_AGENT_PROMPT = "You are a database query assistant. You help users retrieve information about users "
            +
            "\n\nAlways use the basicQuery tool to retrieve information from the database."
            + "Unless there is an error you really only need to use the basicQuery tool one time.";

    public DatabaseQueryAgentPrompt() {
        super(DATABASE_QUERY_AGENT_PROMPT);
    }

    public static String getDefaultPrompt() {
        return DATABASE_QUERY_AGENT_PROMPT;
    }
}