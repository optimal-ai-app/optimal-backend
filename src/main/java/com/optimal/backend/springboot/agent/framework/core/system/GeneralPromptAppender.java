package com.optimal.backend.springboot.agent.framework.core.system;

/**
 * Utility class containing general prompt instructions that should be appended
 * to all system prompts to ensure consistent behavior across all agents and LLM
 * interactions.
 */
public class GeneralPromptAppender {

    /**
     * General instructions to be appended to all system prompts.
     * These instructions ensure the AI behaves consistently and reliably.
     */
    public static final String GENERAL_INSTRUCTIONS = """

           CRITICAL INSTRUCTIONS - ALWAYS FOLLOW:
            - Do NOT hallucinate or make up information
            - Do NOT perform actions outside of what you are explicitly instructed to do by the system
            - Be concise and not verbose
            
            RESPONSE FORMAT REQUIREMENTS:
            {
                "content": "Your response message to the user",
                "tags": ["OPTIONAL_UI_TAG1", "OPTIONAL_UI_TAG2"],
                "readyToHandoff": true/false
            }

            HANDOFF CONTROL INSTRUCTIONS:
            - Use "readyToHandoff": false when you need to:
              * gather information
              * Guide the user through a multi-step process
              * Collect parameters needed for tool execution
              * Continue a conversation to complete your assigned task

            - Use "readyToHandoff": true when you:
              * Have successfully completed your task
              * Have reached a natural conclusion to the conversation
              * MUST ALWAYS hand back control when task finished

            """;

    /**
     * Appends the general instructions to any given system prompt.
     * 
     * @param originalPrompt The original system prompt
     * @return The system prompt with general instructions appended
     */
    public static String appendGeneralInstructions(String originalPrompt) {
        if (originalPrompt == null || originalPrompt.trim().isEmpty()) {
            return GENERAL_INSTRUCTIONS.trim();
        }
        return originalPrompt + GENERAL_INSTRUCTIONS;
    }

    /**
     * Gets just the general instructions without appending to anything.
     * Useful for when you need the instructions as a standalone string.
     * 
     * @return The general instructions string
     */
    public static String getGeneralInstructions() {
        return GENERAL_INSTRUCTIONS;
    }
}