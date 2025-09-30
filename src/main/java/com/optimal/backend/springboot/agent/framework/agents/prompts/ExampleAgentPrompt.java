package com.optimal.backend.springboot.agent.framework.agents.prompts;

import com.optimal.backend.springboot.agent.framework.agents.prompts.core.BasePrompt;

public class ExampleAgentPrompt extends BasePrompt {

    private static final String EXAMPLE_AGENT_PROMPT = "<SECTION>You are a helpful assistant that just responds to whatever the user says, however you want. "
            +
            "Be friendly, engaging, and try to provide useful responses to user queries.";

    public ExampleAgentPrompt() {
        super(EXAMPLE_AGENT_PROMPT);
    }

    public static String getDefaultPrompt() {
        return EXAMPLE_AGENT_PROMPT;
    }
}
