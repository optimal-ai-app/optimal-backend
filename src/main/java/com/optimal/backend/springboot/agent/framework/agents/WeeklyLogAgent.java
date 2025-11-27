package com.optimal.backend.springboot.agent.framework.agents;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.optimal.backend.springboot.agent.framework.agents.prompts.WeeklyLogAgentPrompt;
import com.optimal.backend.springboot.agent.framework.core.BaseAgent;
import com.optimal.backend.springboot.agent.framework.core.LlmClient;

import dev.langchain4j.model.openai.OpenAiChatModel;
import jakarta.annotation.PostConstruct;

@Component
public class WeeklyLogAgent extends BaseAgent {

    @Autowired
    public WeeklyLogAgent(
            @Value("${langchain4j.open-ai.chat-model.api-key:}") String apiKey,
            @Value("Weekly Log Agent") String name,
            @Value("Weekly Log Agent") String description) {
        super(
                name,
                description,
                WeeklyLogAgentPrompt.getDefaultPrompt(),
                                 new LlmClient(
                         OpenAiChatModel.builder()
                                 .apiKey(apiKey)
                                 .modelName("gpt-5-nano")
                                //  .temperature(0.7)
                                //  .maxTokens(1000)
                                // .maxCompletionTokens(1000)
                                 .build()));
    }

    @PostConstruct
    @Override
    protected void initialize() {
        System.out.println("WeeklyLogAgent initialized with tools: " + getTools().size());
        getTools().forEach(tool -> System.out.println("- " + tool.getClass().getSimpleName()));
    }
}
