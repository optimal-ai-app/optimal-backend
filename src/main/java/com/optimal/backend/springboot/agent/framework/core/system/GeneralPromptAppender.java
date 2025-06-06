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
            - Do NOT hallucinate or make up information that wasn't provided
            - Do NOT perform actions outside of what you are explicitly instructed to do by the system
            - Be concise and direct in your responses
            - Be accurate and factual - only use information that is verifiable
            - Double-check your answer before responding
            - If you're unsure about something, state your uncertainty rather than guessing
            - Stick strictly to your assigned role and capabilities
            - Do not add extra features, suggestions, or actions unless specifically requested
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