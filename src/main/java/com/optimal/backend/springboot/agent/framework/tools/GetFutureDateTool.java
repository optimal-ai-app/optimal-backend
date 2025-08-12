package com.optimal.backend.springboot.agent.framework.tools;

import java.time.LocalDate;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.optimal.backend.springboot.agent.framework.core.Tool;

@Component
public class GetFutureDateTool implements Tool {

    @Override
    public String getName() {
        return "get_future_date";
    }

    @Override
    public String getDescription() {
        return "Get the future date";
    }

    @Override
    public String execute(String input) {
        try {
            JsonNode inputNode = new ObjectMapper().readTree(input);
            int days = inputNode.get("days").asInt();
            LocalDate futureDate = LocalDate.now().plusDays(days);
            return futureDate.toString();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }
}
