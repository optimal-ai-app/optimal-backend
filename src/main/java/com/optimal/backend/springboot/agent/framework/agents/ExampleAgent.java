package com.optimal.backend.springboot.agent.framework.agents;

import com.optimal.backend.springboot.agent.framework.core.BaseAgent;
import org.springframework.stereotype.Component;

@Component
public class ExampleAgent extends BaseAgent {

    public ExampleAgent() {
        super("ExampleAgent", "This is an example agent",
         "Just respond to whatever the user says, however you want");
    }

    @Override
    public String getSystemPrompt() {
        return "You are an example agent that just responds to whatever the user says, however you want";
    }

    @Override
    public String getDescription() {
        return "This is an example agent that just responds to whatever the user says, however you want";
    }
    
}
