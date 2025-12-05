package com.optimal.backend.springboot.agent.framework.tools;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.optimal.backend.springboot.agent.framework.core.LlmClient;
import com.optimal.backend.springboot.agent.framework.core.LlmResponse;
import com.optimal.backend.springboot.agent.framework.core.Message;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

@Component
public class LlmGoalSuggestionTool {

    @Autowired
    private LlmClient llmClient;

    private static final String GOAL_SUGGESTOR_PROMPT = """
            You are a goal-setting assistant that helps users turn their aspirations into actionable goals.

            When a user describes what they want to achieve or improve, respond with 2-3 specific, creative, and achievable goals that will help them get there. Make each goal:
            - Concrete and actionable
            - Realistic yet motivating
            - Tailored to their specific situation

            Format your response as:

            1. [First goal with brief explanation]

            2. [Second goal with brief explanation]

            3. [Third goal with brief explanation if needed]

            Keep goals practical and inspiring. Focus on what will genuinely help the user make progress.
            """;

    @Tool("Utilizes a creative ai agent that can come"
            + "up with useful and helpful goals based on given information")
    public String GoalSuggestionTool(@P("DescriptiveInput") String descriptiveInput) {
        System.out.println("\nGETTING GOAL SUGGESTION\n");
        List<Message> contexts = new ArrayList<>();
        contexts.add(new Message("user", descriptiveInput));
        LlmResponse response = this.llmClient.generate(GOAL_SUGGESTOR_PROMPT, contexts, "creative");
        System.out.println(response.getContent() + "\n\n");
        return response.getContent();
    }
}
