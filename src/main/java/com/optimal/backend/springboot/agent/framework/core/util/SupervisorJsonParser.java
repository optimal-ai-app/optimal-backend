package com.optimal.backend.springboot.agent.framework.core.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.LinkedList;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.optimal.backend.springboot.agent.framework.core.AgentNode;
import com.optimal.backend.springboot.agent.framework.core.Message;
import com.optimal.backend.springboot.agent.framework.core.model.SupervisorResponse;

public class SupervisorJsonParser {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static SupervisorResponse parseAgentResponse(String response) {
        try {
            // Try to parse as JSON first
            if (response.trim().startsWith("{") && response.trim().endsWith("}")) {
                JsonNode jsonNode = mapper.readTree(response);
                String content = jsonNode.has("content") ? jsonNode.get("content").asText() : response;

                List<String> tags = new ArrayList<>();
                if (jsonNode.has("tags") && jsonNode.get("tags").isArray()) {
                    for (JsonNode tag : jsonNode.get("tags")) {
                        tags.add(tag.asText());
                    }
                }
                int step = -1;
                if (jsonNode.has("currentStep")) {
                    step = Integer.parseInt(jsonNode.get("currentStep").asText());
                }
                boolean readyToHandoff = jsonNode.has("readyToHandoff") ? jsonNode.get("readyToHandoff").asBoolean()
                        : false;
                Map<String, Object> data = new HashMap<>();
                if (jsonNode.has("data")) {
                    JsonNode dataNode = jsonNode.get("data");
                    data = mapper.convertValue(dataNode, new TypeReference<Map<String, Object>>() {
                    });
                }
                boolean reInterpret = jsonNode.has("reInterpret") ? jsonNode.get("reInterpret").asBoolean() : false;
                return new SupervisorResponse(content, tags, readyToHandoff, data, reInterpret, step);
            }

        } catch (Exception e) {
            // Not JSON, treat as plain text
        }
        // Default response format for plain text
        return new SupervisorResponse(response, new ArrayList<>(), false, new HashMap<>(), false, -1);
    }

    public static Queue<AgentNode> parseAgentNodes(String responseContent) {
        Queue<AgentNode> agentNodes = new LinkedList<>();
        try {
            String cleanedContent = cleanJsonResponse(responseContent);
            JsonNode rootNode = mapper.readTree(cleanedContent);
            if (rootNode.isArray()) {
                for (JsonNode node : rootNode) {
                    AgentNode agentNode = parseAgentNode(node);
                    if (agentNode != null) {
                        agentNodes.add(agentNode);
                    }
                }
            } else if (rootNode.isObject()) {
                AgentNode agentNode = parseAgentNode(rootNode);
                if (agentNode != null) {
                    agentNodes.add(agentNode);
                }
            } else {
                throw new RuntimeException(
                        "Invalid response format: Expected JSON object or array, got: " + rootNode.getNodeType());
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse agent nodes: " + e.getMessage(), e);
        }
        return agentNodes;
    }

    public static String extractFinalResponse(List<Message> messages) {
        // Get the last assistant message from the conversation
        for (int i = messages.size() - 1; i >= 0; i--) {
            Message msg = messages.get(i);
            if ("assistant".equals(msg.getRole()) && msg.getContent() != null) {
                return msg.getContent();
            }
        }
        return "No response generated";
    }

    /**
     * Clean JSON response by removing markdown code blocks if present
     */
    public static String cleanJsonResponse(String responseContent) {
        if (responseContent == null || responseContent.trim().isEmpty()) {
            return "[]"; // Return empty array as fallback
        }

        String cleaned = responseContent.trim();
        // Remove all occurrences of code block markers (```json, ```JSON, and ```)
        cleaned = cleaned.replaceAll("(?i)```json", "");
        cleaned = cleaned.replaceAll("(?i)```", "");

        return cleaned.trim();
    }

    /*
     * This takes JSON agent node and returns an AgentNode object
     */
    private static AgentNode parseAgentNode(JsonNode node) {
        if (!node.has("name") || !node.has("instruction")) {
            return null;
        }
        String name = node.get("name").asText();
        String instruction = node.get("instruction").asText();
        HashSet<String> dependencies = new HashSet<>();
        JsonNode depsNode = node.get("dependency");
        if (depsNode != null && depsNode.isArray()) {
            for (JsonNode dep : depsNode) {
                dependencies.add(dep.asText());
            }
        }
        return new AgentNode(name, dependencies, instruction);
    }
}
