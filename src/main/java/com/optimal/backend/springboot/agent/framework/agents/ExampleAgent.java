package com.optimal.backend.springboot.agent.framework.agents;

import com.optimal.backend.springboot.agent.framework.agents.prompts.ExampleAgentPrompt;
import com.optimal.backend.springboot.agent.framework.core.BaseAgent;
import org.springframework.stereotype.Component;

@Component
public class ExampleAgent extends BaseAgent {

    public ExampleAgent() {
        super("ExampleAgent", "This is an example agent that just responds to whatever the user says, however you want",
                ExampleAgentPrompt.getDefaultPrompt());
    }
}
