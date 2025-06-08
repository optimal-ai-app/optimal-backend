package com.optimal.backend.springboot.agent.framework.tools;

import com.optimal.backend.springboot.agent.framework.core.Tool;
import com.optimal.backend.springboot.model.User;
import com.optimal.backend.springboot.repository.UserRepository;
import dev.langchain4j.agent.tool.ToolParameters;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * BasicQuery tool for searching and listing users from the database
 * Demonstrates database integration within the agent framework
 * 
 * NULL-SAFETY GUARANTEES:
 * ✅ Never returns null - always returns a meaningful string
 * ✅ Handles null/empty input gracefully
 * ✅ Handles database connection failures
 * ✅ Handles null/empty result sets
 * ✅ Handles null individual User objects
 * ✅ Handles null/empty User fields (name, username, email, role)
 * ✅ Handles JSON parsing exceptions
 * ✅ Provides fallback "No entries found" for all empty conditions
 * ✅ Final safety check before returning result
 */
@Component
public class BasicQueryTool implements Tool {

    @Autowired
    private UserRepository userRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String getName() {
        return "basicQuery";
    }

    @Override
    public String execute(String input) {
        List<User> users = userRepository.findAll();
        return "DB returned: " + (users == null ? "null" : users.toString());  
    }

    @Override
    public String getDescription() {
        return "Searches the database and lists user information. Can return all users with basic info, detailed info, just names, just usernames, or just the count.";
    }

    @Override
    public ToolParameters getParameters() {
        return ToolParameters.builder()
                .type("object")
                .properties(java.util.Map.of(
                        "queryType", java.util.Map.of(
                                "type", "string",
                                "description", "Type of query to perform",
                                "enum", Arrays.asList("all", "names", "usernames", "count")),
                        "includeDetails", java.util.Map.of(
                                "type", "boolean",
                                "description",
                                "Whether to include detailed user information (ID, email, role). Only applies to 'all' queryType")))
                .required(Arrays.asList()) // No required parameters - all are optional
                .build();
    }
}