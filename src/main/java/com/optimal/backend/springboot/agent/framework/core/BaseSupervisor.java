package com.optimal.backend.springboot.agent.framework.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.optimal.backend.springboot.agent.framework.core.interfaces.SupervisorInterface;
import com.optimal.backend.springboot.agent.framework.core.system.GeneralPromptAppender;

@Component
public class BaseSupervisor implements SupervisorInterface {
    private final Map<String, BaseAgent> agents = new HashMap<>();

    @Autowired
    private LlmClient llmClient;

    private static final String INTERPRETER_PROMPT_BASE = """
            You are an intelligent task coordinator that analyzes user requests and determines which agents should execute to fulfill the request.

            Your job is to:
            1. Analyze the user's input to understand what they want to accomplish
            2. Determine which agents are needed to complete the task
            3. Identify any dependencies between agents (which agents must finish before others can start)
            4. Provide specific instructions for each agent
            """;

    private static final String INTERPRETER_PROMPT_RULES = """
            You MUST respond with a valid JSON array of agent nodes. Each agent node must have this exact structure:
            {
                "name": "AgentName",
                "instruction": "Specific instruction for this agent",
                "dependency": ["AgentName1", "AgentName2"]
            }

            Rules:
            - The array CANNOT be empty, if there is only one agent, return an array with that agent
            - "name" must be exactly one of the available agent names listed above
            - "instruction" should be a clear, specific task for that agent
            - "dependency" is an array of agent names that must complete before this agent can start (empty array [] if no dependencies)
            - The dependency system creates a Directed Acyclic Graph (DAG) - agents with no dependencies run first, then agents whose dependencies are complete
            - DO NOT create circular dependencies
            - ONLY use agent names that exist in the available agents list above

            Always respond with valid JSON only - no additional text or explanation.
            """;

    private static final String SUMMARIZER_PROMPT = """
            You are a concise summarizer that reviews the entire conversation between the user and agents, then provides a brief, clear summary that includes the substance of each agent's responses.

            Your task:
            1. Review all messages in the conversation context.
            2. Identify actions taken by each agent and capture the actual content of their outputs.
            3. Provide a concise, user-friendly summary of results, quoting short outputs verbatim.
            4. Focus on concrete outcomes and deliverables, not internal steps.

            Guidelines:
            - Use first person ("I") when describing actions taken on behalf of the user.
            - For outputs up to ~50 words, include them verbatim in quotes.
            - For longer outputs, summarize the key points in 1–2 sentences.
            - Mention quantities when relevant (e.g., "I created 5 todo items").
            - Keep it conversational and friendly.
            - If something failed or had errors, note it briefly but emphasize successes.
            - Aim for 1–3 sentences maximum.
            """;

    public void addAgent(String name, BaseAgent agent) {
        agents.put(name, agent);
    }

    @Override
    public String execute(List<Message> userInput) {
        Queue<AgentNode> queue = interpret(userInput);
        Map<String, List<Message>> agentOutputs = new HashMap<>();
        Set<String> finishedAgents = new HashSet<>();
        Set<String> processingAgents = new HashSet<>();
        int maxIterations = queue.size() * 2;
        int iterations = 0;

        agentOutputs.put("user", userInput);

        while (!queue.isEmpty() && iterations < maxIterations) {
            iterations++;
            AgentNode current = queue.poll();
            String agentName = current.getName();

            if (finishedAgents.contains(agentName)) {
                continue;
            }

            if (!processingAgents.add(agentName)) {
                throw new IllegalStateException("Dependency cycle detected at agent: " + agentName);
            }

            if (areDependenciesMet(current, finishedAgents)) {
                List<Message> agentContext = buildAgentContext(current, agentOutputs);
                
                BaseAgent agent = agents.get(agentName);
                List<Message> response = agent.run(agentContext);
                agentOutputs.put(agentName, response);
                finishedAgents.add(agentName);
                processingAgents.remove(agentName);
            } else {
                queue.add(current);
            }
        }

        if (iterations >= maxIterations) {
            throw new IllegalStateException("Execution exceeded maximum iterations. Possible circular dependency.");
        }

        return summarizeResults(agentOutputs);
    }

    @Override
    public Queue<AgentNode> interpret(List<Message> userInput) {
        List<Message> contexts = userInput;
        String systemPrompt = buildInterpreterPrompt();
        
        LlmResponse response = llmClient.generate(systemPrompt, contexts, new ArrayList<>());
        return parseAgentNodes(response.getContent());
    }

    @Override
    public String summarize(List<Message> contexts) {
        String enhancedPrompt = GeneralPromptAppender.appendGeneralInstructions(SUMMARIZER_PROMPT);
        LlmResponse response = llmClient.generate(enhancedPrompt, contexts, new ArrayList<>());
        return response.getContent();
    }

    private String buildInterpreterPrompt() {
        StringBuilder prompt = new StringBuilder(INTERPRETER_PROMPT_BASE);
        prompt.append("\nAvailable agents and their capabilities:\n");

        if (agents.isEmpty()) {
            prompt.append("- No agents are currently registered\n");
        } else {
            agents.forEach((name, agent) -> {
                String description = agent.getDescription();
                if (description == null || description.trim().isEmpty()) {
                    description = "Agent for handling " + name.toLowerCase() + " related tasks";
                }
                prompt.append("- ").append(name).append(": ").append(description).append("\n");
            });
        }

        prompt.append(INTERPRETER_PROMPT_RULES);
        return GeneralPromptAppender.appendGeneralInstructions(prompt.toString());
    }

    private boolean areDependenciesMet(AgentNode current, Set<String> finishedAgents) {
        return current.getDependencies().stream().allMatch(finishedAgents::contains);
    }

    private List<Message> buildAgentContext(AgentNode current, Map<String, List<Message>> agentOutputs) {
        List<Message> context = new ArrayList<>(current.getInstructions());
        
        for (String dep : current.getDependencies()) {
            context.addAll(agentOutputs.getOrDefault(dep, List.of()));
        }
        
        if (current.getDependencies().isEmpty()) {
            context.addAll(agentOutputs.get("user"));
        }
        
        return context;
    }

    private String summarizeResults(Map<String, List<Message>> agentOutputs) {
        List<Message> summaryContext = new ArrayList<>(agentOutputs.get("user"));
        agentOutputs.entrySet().stream()
                .filter(e -> !e.getKey().equals("user"))
                .forEach(e -> summaryContext.addAll(e.getValue()));
        
        return summarize(summaryContext);
    }

    private Queue<AgentNode> parseAgentNodes(String responseContent) {
        Queue<AgentNode> agentNodes = new LinkedList<>();
        
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode[] nodes = mapper.readValue(responseContent, JsonNode[].class);
            
            for (JsonNode node : nodes) {
                String name = node.get("name").asText();
                String instruction = node.get("instruction").asText();

                HashSet<String> dependencies = new HashSet<>();
                JsonNode depsNode = node.get("dependency");
                if (depsNode != null && depsNode.isArray()) {
                    for (JsonNode dep : depsNode) {
                        dependencies.add(dep.asText());
                    }
                }

                agentNodes.add(new AgentNode(name, dependencies, instruction));
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse agent nodes", e);
        }

        return agentNodes;
    }
}
