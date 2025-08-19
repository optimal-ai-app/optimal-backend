package com.optimal.backend.springboot.agent.framework.tools;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.optimal.backend.springboot.agent.framework.core.Tool;

import dev.langchain4j.agent.tool.ToolParameters;

@Component
public class GetFutureDateTool implements Tool {

    @Override
    public String getName() {
        return "get_future_date";
    }

    @Override
    public String getDescription() {
        return "Returns an ISO date (yyyy-MM-dd) that is N days after today. Requires parameter 'days' (integer).";
    }

    @Override
    public String execute(String input) {
        try {
            if (input == null || input.trim().isEmpty()) {
                return "Error: missing required parameter 'days' (integer).";
            }
            JsonNode inputNode = new ObjectMapper().readTree(input);
            if (inputNode == null || !inputNode.has("days") || inputNode.get("days").isNull()) {
                return "Error: missing required parameter 'days' (integer).";
            }
            int days = inputNode.get("days").asInt();
            LocalDate futureDate = LocalDate.now().plusDays(days);
            return futureDate.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }

    @Override
    public ToolParameters getParameters() {
        return ToolParameters.builder()
                .type("object")
                .properties(Map.of(
                        "days", Map.of("type", "integer", "description", "Number of days in the future from today")))
                .required(Arrays.asList("days"))
                .build();
    }
}
