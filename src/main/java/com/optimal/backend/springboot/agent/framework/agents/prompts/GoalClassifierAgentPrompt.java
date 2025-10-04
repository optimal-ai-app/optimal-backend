package com.optimal.backend.springboot.agent.framework.agents.prompts;

import com.optimal.backend.springboot.agent.framework.agents.prompts.core.BasePrompt;

public class GoalClassifierAgentPrompt extends BasePrompt {

    private static final String GOAL_CLASSIFIER_PROMPT = """
            Given basic information about a goal. Your job is to determine whether the goal is a quantitative goal or a qualitative goal based on the description
            of a goal.
            Here are definitions of a quantitative goal and qualitative goal with an example:

            Quantitative Goal
            Definition: Based on units of measure (distance, count, time, money, pages, etc.). Success = reaching a measurable quantity.
            Example: Run 100 miles in 30 days.
            Example: Read 300 pages this month.

            Qualitative Goal
            Definition: Process-focused, about behavior, learning, or improvement without unit-based measurement. Deadlines may exist but do not make it quantitative.
            Example: Train consistently to run a 5k by the end of the year.
            Example: Improve public speaking skills through weekly practice.

            Rule for classification
            If progress is tracked in countable units → Quantitative.
            If progress is tracked in practice, process, or experience (even with a deadline) → Qualitative.

            You must respond in the following format:

            {
                "type" : ["qualitative" or "quantitative"],
                "metric" : [numeric metric for quantitative goal, do not include if goal is qualitative]
            }

            Examples:

            Quantitative goal: Read a 300 page book.
            {
                "type" : "quantitative"
                "metric" : 300
            }

            Qualitative goal: Improve customer satisfaction by enhancing the support team’s communication skills
            {
                "type" : "qualitative"
            }
            """;

    public GoalClassifierAgentPrompt() {
        super(GOAL_CLASSIFIER_PROMPT);
    }

    public static String getDefaultPrompt() {
        return GOAL_CLASSIFIER_PROMPT;
    }

}
