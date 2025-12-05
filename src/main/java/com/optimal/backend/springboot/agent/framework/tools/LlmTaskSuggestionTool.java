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
public class LlmTaskSuggestionTool {

    @Autowired
    private LlmClient llmClient;

    private static final String TASK_SUGGESTOR_PROMPT = """
            You are a task-planning assistant that helps users create actionable, repeating tasks.

            When given information about what the user wants to accomplish (which may include goals, milestones, user queries, or other context), respond with ONE specific, creative, and achievable repeating task. Make the task:
            - Concrete and actionable (follow format: [ACTION] + [DELIVERABLE] + [MEASURABLE OUTCOME])
            - Realistic yet motivating
            - Tailored to the specific context provided
            - Designed as a repeating task that builds progress over time
            - Practical and focused on meaningful actions

            **CRITICAL CONSTRAINTS:**
            - The task MUST be a REGULAR task (not a milestone)
            - Extract frequency information from the input if available (e.g., "3 times a week" → suggest appropriate repeat days)
            - Avoid planning, organizing, research, meta, or duplicate task types
            - Consider existing tasks mentioned in the context to avoid duplicates

            Format your response as a single task suggestion with:
            - Task type (short action title)
            - Task description (detailed action)
            - Suggested repeat days (e.g., ["M","W","F"] for 3 times a week)
            - Suggested time of day (HH:MM format)
            - Priority level (!!! for high, !! for medium, ! for low)

            Keep the task practical and inspiring. Focus on what will genuinely help the user make progress.
            """;

    @Tool("Utilizes a creative ai agent that can come"
            + "up with useful and helpful tasks based on given information")
    public String TaskSuggestionTool(@P("DescriptiveInput") String descriptiveInput) {
        List<Message> contexts = new ArrayList<>();
        contexts.add(new Message("user", descriptiveInput));
        LlmResponse response = this.llmClient.generate(TASK_SUGGESTOR_PROMPT, contexts, "creative");
        System.out.println(response.getContent() + "\n\n");
        return response.getContent();
    }
}
