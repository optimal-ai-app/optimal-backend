package com.optimal.backend.springboot.agent.framework.agents;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.optimal.backend.springboot.agent.framework.core.BaseAgent;
import com.optimal.backend.springboot.agent.framework.core.LlmClient;

import jakarta.annotation.PostConstruct;

@Component
public class DefaultAgent extends BaseAgent {

    @Autowired
    public DefaultAgent(
            @Value("DefaultAgent") String name,
            @Value("DefaultAgent is an agent that helps direct the user into workflows that exist in the application") String description,
            LlmClient llmClient) {
        super(name, description, "", llmClient);
    }

    @PostConstruct
    @Override
    protected void initialize() {
        System.out.println("ContextAgent initialized with tools: " + getTools().size());
        getTools().forEach(tool -> System.out.println("- " + tool.getClass().getSimpleName()));
    }
}