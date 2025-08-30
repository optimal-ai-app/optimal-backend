package com.optimal.backend.springboot.agent.framework.agents;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.optimal.backend.springboot.agent.framework.agents.prompts.DiaryLogAgentPrompt;
import com.optimal.backend.springboot.agent.framework.core.BaseAgent;
import com.optimal.backend.springboot.agent.framework.core.LlmClient;
import com.optimal.backend.springboot.agent.framework.tools.GetGoalDescriptionTool;
import com.optimal.backend.springboot.agent.framework.tools.GetTasksforGoalTool;

import dev.langchain4j.model.openai.OpenAiChatModel;
import jakarta.annotation.PostConstruct;

@Component
public class DiaryLogAgent extends BaseAgent {
    @SuppressWarnings("unused")
    private final GetGoalDescriptionTool goalDescriptionTool;
    @SuppressWarnings("unused")
    private final GetTasksforGoalTool getTasksforGoalTool;

    @Autowired
    public DiaryLogAgent(
            @Value("${langchain4j.open-ai.chat-model.api-key:}") String apiKey,
            @Value("${langchain4j.diary-log-agent.name}") String name,
            @Value("${langchain4j.diary-log-agent.description}") String description,
            GetGoalDescriptionTool goalDescriptionTool, GetTasksforGoalTool getTasksforGoalTool) {
        super(
                name,
                description,
                DiaryLogAgentPrompt.getDefaultPrompt(),
                new LlmClient(
                        OpenAiChatModel.builder()
                                .apiKey(apiKey)
                                .modelName("gpt-4o-mini")
                                .temperature(0.3)
                                .maxTokens(2000)
                                .build()));
        this.goalDescriptionTool = goalDescriptionTool;
        this.getTasksforGoalTool = getTasksforGoalTool;
        addTool(goalDescriptionTool);
        addTool(getTasksforGoalTool);
    }

    @PostConstruct
    @Override
    protected void initialize() {
        System.out.println("DiaryLogAgent initialized with tools: " + getTools().size());
        getTools().forEach(tool -> System.out.println("- " + tool.getClass().getSimpleName()));
    }
}
