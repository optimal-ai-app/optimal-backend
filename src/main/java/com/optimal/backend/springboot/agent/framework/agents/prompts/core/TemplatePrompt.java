package com.optimal.backend.springboot.agent.framework.agents.prompts.core;

import java.util.Map;

/**
 * Advanced prompt class that supports template variables
 * Useful for dynamic prompts that need contextual information
 */
public abstract class TemplatePrompt extends BasePrompt {

    public TemplatePrompt(String template) {
        super(template);
    }

    /**
     * Generate a prompt by replacing placeholders with actual values
     * 
     * @param variables Map of placeholder names to their values
     * @return The prompt with placeholders replaced
     */
    public String getPrompt(Map<String, String> variables) {
        String result = this.prompt;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            result = result.replace(placeholder, entry.getValue());
        }
        return result;
    }

    /**
     * Get available placeholder names in the template
     * 
     * @return Array of placeholder names (without the {{ }} syntax)
     */
    public abstract String[] getPlaceholders();
}