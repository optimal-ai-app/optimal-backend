package com.optimal.backend.springboot.agent.framework.agents;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.optimal.backend.springboot.agent.framework.agents.prompts.ContextAgentPrompt;
import com.optimal.backend.springboot.agent.framework.core.BaseAgent;
import com.optimal.backend.springboot.agent.framework.core.LlmClient;

import jakarta.annotation.PostConstruct;

@Component
public class ContextAgent extends BaseAgent {


    @Autowired
    public ContextAgent(
            @Value("ContextAgent") String name,
            @Value("ContextAgent is an agent that gets the chat logs of a conversation between a user and another agent."+
            "Your job is to develop a summary of the conversation so that the next time the agent is called, they can use"+
            " the summary to get back to the same point in the conversation they were at.") String description,
            LlmClient llmClient) {
        super(name, description, ContextAgentPrompt.getDefaultPrompt(), llmClient);
    }

    @PostConstruct
    @Override
    protected void initialize() {
        System.out.println("TaskPlannerAgent initialized with tools: " + getTools().size());
        getTools().forEach(tool -> System.out.println("- " + tool.getClass().getSimpleName()));
    }
}