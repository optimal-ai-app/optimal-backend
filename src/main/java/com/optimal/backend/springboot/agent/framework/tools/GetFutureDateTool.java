package com.optimal.backend.springboot.agent.framework.tools;

import java.util.Date;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.optimal.backend.springboot.agent.framework.core.Tool;
import com.optimal.backend.springboot.utils.DateUtils;

@Component
public class GetFutureDateTool implements Tool {

    private final ObjectMapper objectMapper = new ObjectMapper();

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
            Date futureDate = DateUtils.getCurrentDatePlusDays(days);
            return objectMapper.writeValueAsString(futureDate);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }
}
