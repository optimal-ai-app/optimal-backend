package com.optimal.backend.springboot.agent.framework.core;

import dev.langchain4j.agent.tool.ToolParameters;
import dev.langchain4j.agent.tool.JsonSchemaProperty;

/**
 * Interface that all tools must implement to be used by agents
 * Enhanced to support LangChain4j tool specifications with parameters
 */
public interface Tool {
    /**
     * Get the name of the tool
     * 
     * @return The tool name used for identification
     */
    String getName();

    /**
     * Execute the tool with the given input JSON string
     * 
     * IMPORTANT: This method MUST NEVER return null. Always return a meaningful
     * string:
     * - On success: return the actual result
     * - On empty results: return "No entries found" or similar
     * - On errors: return descriptive error message
     * 
     * @param input The input parameters as JSON string (may be null or empty)
     * @return The output result from executing the tool (NEVER null)
     */
    String execute(String input);

    /**
     * Get a description of what this tool does
     * 
     * @return Description of the tool's functionality
     */
    String getDescription();

    /**
     * Get the parameters specification for this tool
     * Used by LangChain4j to generate proper tool specifications
     * 
     * @return ToolParameters object defining the tool's input schema
     */
    default ToolParameters getParameters() {
        // Default implementation returns empty parameters
        return ToolParameters.builder().build();
    }

    /**
     * Helper method to create string parameters for tools
     * 
     * @param name        Parameter name
     * @param description Parameter description
     * @param required    Whether the parameter is required
     * @return JsonSchemaProperty for string parameter
     */
    default JsonSchemaProperty stringParameter(String name, String description, boolean required) {
        return JsonSchemaProperty.STRING.description(description);
    }

    /**
     * Helper method to create number parameters for tools
     * 
     * @param name        Parameter name
     * @param description Parameter description
     * @param required    Whether the parameter is required
     * @return JsonSchemaProperty for number parameter
     */
    default JsonSchemaProperty numberParameter(String name, String description, boolean required) {
        return JsonSchemaProperty.NUMBER.description(description);
    }

    /**
     * Helper method to create boolean parameters for tools
     * 
     * @param name        Parameter name
     * @param description Parameter description
     * @param required    Whether the parameter is required
     * @return JsonSchemaProperty for boolean parameter
     */
    default JsonSchemaProperty booleanParameter(String name, String description, boolean required) {
        return JsonSchemaProperty.BOOLEAN.description(description);
    }

    /**
     * Helper method to create enum parameters for tools
     * 
     * @param name        Parameter name
     * @param description Parameter description
     * @param values      Possible enum values
     * @return JsonSchemaProperty for enum parameter
     */
    default JsonSchemaProperty enumParameter(String name, String description, String... values) {
        return JsonSchemaProperty.STRING.description(description).enums((Object[]) values);
    }
}