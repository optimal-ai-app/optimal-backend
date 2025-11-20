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
public class LlmMilestoneSuggestionTool {

    @Autowired
    private LlmClient llmClient;

    private static final String MILESTONE_SUGGESTOR_PROMPT = """
            You are a milestone-planning assistant that helps users break down their goals into actionable milestones.

            When a user describes a goal they want to achieve, respond with specific, creative, and achievable milestones that will help them progress toward that goal. Make each milestone:
            - Concrete and actionable (follow format: [ACTION] + [DELIVERABLE] + [MEASURABLE OUTCOME])
            - Realistic yet motivating
            - Tailored to their specific goal and timeline
            - Progressively building toward the final goal
            - Include realistic due dates in YYYY-MM-DD format

            **CRITICAL CONSTRAINTS:**
            - ALL milestone due dates MUST be on or before the goal's due date
            - If no milestones exist for the goal: propose 3-5 natural progression milestones with realistic due dates that are evenly spaced between the current date and the goal's due date
            - If milestones already exist: suggest 1-3 fitting next steps that build on existing progress, ensuring dates are before the goal's due date

            Format your response as:

            1. [First milestone with brief explanation] by YYYY-MM-DD

            2. [Second milestone with brief explanation] by YYYY-MM-DD

            3. [Third milestone with brief explanation] by YYYY-MM-DD

            (Continue with 4-5 milestones if appropriate, or fewer if suggesting next steps)

            Keep milestones practical and inspiring. Focus on what will genuinely help the user make progress toward their goal.
            Avoid planning, organizing, research, meta, or duplicate milestone types.
            """;

    @Tool("Utilizes a creative ai agent that can come"
            + "up with useful and helpful milestones based on given goal information")
    public String MilestoneSuggestionTool(@P("DescriptiveInput") String descriptiveInput) {
        System.out.println("\nGETTING MILESTONE SUGGESTION\n");
        List<Message> contexts = new ArrayList<>();
        contexts.add(new Message("user", descriptiveInput));
        LlmResponse response = this.llmClient.generate(MILESTONE_SUGGESTOR_PROMPT, contexts, "creative");
        System.out.println(response.getContent() + "\n\n");
        return response.getContent();
    }
}

