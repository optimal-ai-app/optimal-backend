package com.optimal.backend.springboot.agent.framework.agents;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.optimal.backend.springboot.agent.framework.agents.prompts.GoalClassifierAgentPrompt;
import com.optimal.backend.springboot.agent.framework.core.BaseAgent;
import com.optimal.backend.springboot.agent.framework.core.LlmClient;

import dev.langchain4j.model.openai.OpenAiChatModel;
import jakarta.annotation.PostConstruct;

@Component
public class GoalClassifierAgent extends BaseAgent {

    @Autowired
    public GoalClassifierAgent(
            @Value("${langchain4j.open-ai.chat-model.api-key:}") String apiKey,
            @Value("Goal Classifier Agent") String name,
            @Value("Goal Classifier Agent") String description) {
        super(
                name,
                description,
                GoalClassifierAgentPrompt.getDefaultPrompt(),
                new LlmClient(
                        OpenAiChatModel.builder()
                                .apiKey(apiKey)
                                .modelName("gpt-4.1-nano")
                                .temperature(0.3)
                                // .maxTokens(1000)
                                // .maxCompletionTokens(1000)
                                .build()));
    }

    @PostConstruct
    @Override
    protected void initialize() {
        System.out.println("GoalClassifierAgent initialized with tools: " + getTools().size());
        getTools().forEach(tool -> System.out.println("- " + tool.getClass().getSimpleName()));
    }
}
