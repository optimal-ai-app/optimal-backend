package com.optimal.backend.springboot.agent.framework.agents.prompts;

import com.optimal.backend.springboot.agent.framework.agents.prompts.core.BasePrompt;
import com.optimal.backend.springboot.agent.framework.core.system.GeneralPromptAppender;

public class InstructionAgentPrompt extends BasePrompt {
    public static final String PROMPT = """
                        Task
            Read last 2 messages + system prompt. Return the ONE most relevant instruction for the next response.
            Rules

            If user asks for unsupported action → return reinterpret: true
            Find current position in workflow/steps
            Return appropriate step:

            User stuck/confused → previous step
            User completed task → next step
            User needs clarification → current step
            User starting → first step



            Output
            Return instruction text exactly as written, OR reinterpret: true
            Input
            Messages: [INSERT_LAST_2_MESSAGES]
            System: [INSERT_SYSTEM_PROMPT]
                        """;

    public InstructionAgentPrompt() {
        super(PROMPT);
    }

    public static String getDefaultPrompt() {
        return GeneralPromptAppender.appendGeneralInstructions(PROMPT);
    }
}
