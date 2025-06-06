package com.optimal.backend.springboot.agent.framework.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.optimal.backend.springboot.agent.framework.core.interfaces.SupervisorInterface;

public class BaseSupervisor implements SupervisorInterface {
    Map<String, BaseAgent> agents = new HashMap<>();

    public void addAgent(String name, BaseAgent agent) {
        agents.put(name, agent);
    }

    public String execute(String userInput) {
        Queue<AgentNode> queue = interpret(userInput);
        List<Message> contexts = new ArrayList<>();
        contexts.add(new Message("user", userInput));
        while (!queue.isEmpty()) {
            AgentNode current = queue.poll();
            if (current.getDependencies().isEmpty()) {
                BaseAgent agent = agents.get(current.getName());
                contexts.addAll(current.getInstructions());
                List<Message> response = agent.run(current.getInstructions());
                contexts.addAll(response);
            } else {
                queue.add(current);
            }
        }
        String output = summarize(contexts);
        return output;
    }

    public Queue<AgentNode> interpret(String userInput) {
        LlmClient llmClient = new LlmClient(null);
        String prompt = "Interpret the following user input: " + userInput; // TODO: Add more context to the prompt
        List<Message> contexts = new ArrayList<>();
        contexts.add(new Message("user", userInput));
        LlmResponse response = llmClient.generate(prompt, contexts, new ArrayList<>());
        String responseContent = response.getContent();
        Queue<AgentNode> agentNodes = new LinkedList<AgentNode>();
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode[] nodes = mapper.readValue(responseContent, JsonNode[].class);
            for (JsonNode node : nodes) {
                String name = node.get("name").asText();
                String instruction = node.get("instruction").asText();

                HashSet<String> dependencies = new HashSet<>();
                JsonNode depsNode = node.get("dependency");
                if (depsNode.isArray()) {
                    for (JsonNode dep : depsNode) {
                        dependencies.add(dep.asText());
                    }
                }

                AgentNode agentNode = new AgentNode(name, dependencies, instruction);
                agentNodes.add(agentNode);
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse agent nodes", e);
        }

        return agentNodes;
    }

    public String summarize(List<Message> contexts) {
        return "Summarized output";
    }

}
