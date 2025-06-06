package com.optimal.backend.springboot.agent.framework.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.optimal.backend.springboot.agent.framework.core.interfaces.SupervisorInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BaseSupervisor implements SupervisorInterface {
    private final Map<String, BaseAgent> agents = new HashMap<>();

    @Autowired
    private LlmClient llmClient;

    public void addAgent(String name, BaseAgent agent) {
        agents.put(name, agent);
    }

    @Override
    public String execute(String userInput) {
        Queue<AgentNode> queue = interpret(userInput);
        List<Message> contexts = new ArrayList<>();
        contexts.add(new Message("user", userInput));

        Set<String> finishedAgents = new HashSet<>();

        while (!queue.isEmpty()) {
            AgentNode current = queue.poll();
            if (finishedAgents.containsAll(current.getDependencies())) {
                BaseAgent agent = agents.get(current.getName());

                // Add this agent's instructions into the shared context
                contexts.addAll(current.getInstructions());

                // Pass the entire conversation history (contexts) to run()
                List<Message> response = agent.run(new ArrayList<>(contexts));
                contexts.addAll(response);

                finishedAgents.add(current.getName());
            } else {
                queue.add(current);
            }
        }

        return summarize(contexts);
    }

    @Override
    public Queue<AgentNode> interpret(String userInput) {
        String prompt = "Interpret the following user input: " + userInput; // TODO: Add more context to the prompt
        List<Message> contexts = new ArrayList<>();
        contexts.add(new Message("user", userInput));

        LlmResponse response = llmClient.generate(prompt, contexts, new ArrayList<>());
        String responseContent = response.getContent();

        Queue<AgentNode> agentNodes = new LinkedList<>();
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

    @Override
    public String summarize(List<Message> contexts) { // TODO: Implement this
        return "Summarized output";
    }
}
