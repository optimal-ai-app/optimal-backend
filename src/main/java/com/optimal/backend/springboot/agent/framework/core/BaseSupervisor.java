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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.optimal.backend.springboot.agent.framework.core.interfaces.SupervisorInterface;
import com.optimal.backend.springboot.agent.framework.core.system.GeneralPromptAppender;

public class BaseSupervisor implements SupervisorInterface {
    private final Map<String, BaseAgent> agents = new HashMap<>();

    private String handoffAgent = null;

    @Autowired
    private LlmClient llmClient;

    // Constructor for manual dependency injection
    public BaseSupervisor(LlmClient llmClient) {
        this.llmClient = llmClient;
    }

    // Default constructor for Spring dependency injection
    public BaseSupervisor() {
        // LlmClient will be injected by Spring via @Autowired
    }

    private static final String INTERPRETER_PROMPT_BASE = """
            You are an intelligent task coordinator that analyzes user requests and determines which agents should execute to fulfill the request.

            Your job is to:
            1. Analyze the user's input to understand what they want to accomplish
            2. Determine which agents are needed to complete the task
            3. Identify any dependencies between agents (which agents must finish before others can start)
            4. Provide specific instructions for each agent

            AGENT SELECTION INTELLIGENCE:

            **TaskAgent** - Use when user wants to:
            - Create tasks, todos, or action items
            - Break down goals into actionable steps
            - Set up recurring activities or habits
            - Schedule specific work or study sessions
            - Plan workout routines, study schedules, etc.
            - Anything involving "task", "todo", "schedule", "plan", "routine"

            INSTRUCTION INTELLIGENCE:
            - Be SPECIFIC about what the agent should accomplish
            - Include relevant context from the user's request
            - Reference specific goals, targets, or requirements mentioned
            - Don't just say "handle the user's request" - explain WHAT specifically to do
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

    private static final String SUMMARIZER_PROMPT = String.format(
            """
                    You are a concise summarizer that reviews the entire conversation between the user and agents.

                    1. a brief, user-friendly string describing what was done.

                    Rules:
                    - "summary" should use first-person ("I created 3 goals…").
                    """);

    public void addAgent(String name, BaseAgent agent) {
        agents.put(name, agent);
    }

    // New response class for handoff mechanism
    public static class SupervisorResponse {
        public String content;
        public List<String> tags;
        public boolean readyToHandoff;
        public Map<String, Object> data;

        public SupervisorResponse(String content, List<String> tags, boolean readyToHandoff, Map<String, Object> data) {
            this.content = content;
            this.tags = tags != null ? tags : new ArrayList<>();
            this.readyToHandoff = readyToHandoff;
            this.data = data;
        }
    }

    // Method to execute with handoff support
    public SupervisorResponse executeWithHandoff(List<Message> userInput) {
        // If there's an active handoff agent, execute it directly
        if (handoffAgent != null && agents.containsKey(handoffAgent)) {
            return executeHandoffAgent(userInput);
        }

        // Normal supervisor execution
        return executeNormal(userInput);
    }

    private SupervisorResponse executeHandoffAgent(List<Message> userInput) {
        BaseAgent agent = agents.get(handoffAgent);
        if (agent == null) {
            // Agent not found, clear handoff and return to normal execution
            handoffAgent = null;
            return executeNormal(userInput);
        }

        try {
            List<Message> response = agent.run(userInput);
            String finalResponse = extractFinalResponse(response);

            // Parse the agent response to check for handoff decision
            SupervisorResponse parsedResponse = parseAgentResponse(finalResponse);

            // If agent is ready to handoff, clear the handoff agent and return the result
            // DO NOT call executeNormal again as this causes duplicate execution
            if (parsedResponse.readyToHandoff) {
                System.out.println("Agent " + handoffAgent + " completed successfully, clearing handoff");
                handoffAgent = null;
                return parsedResponse; // Return the result immediately
            }

            // If agent is not ready to handoff, keep the handoff agent active
            System.out.println("Agent " + handoffAgent + " keeping control (readyToHandoff=false)");
            return parsedResponse;
        } catch (Exception e) {
            System.out.println("Error in handoff agent " + handoffAgent + ": " + e.getMessage());
            // On error, clear handoff and return control to supervisor
            handoffAgent = null;
            return executeNormal(userInput);
        }
    }

    private SupervisorResponse executeNormal(List<Message> userInput) {
        System.out.println("\n\nHandoff to Supervisor\n\n");

        // First, ask the LLM which agents should handle this request
        System.out.println("Supervisor determining which agents to use...");
        Queue<AgentNode> queue = interpret(userInput);

        // Check if interpretation was successful
        if (queue.isEmpty()) {
            System.err.println("Failed to interpret user request - no agents selected");
            return new SupervisorResponse("I'm not sure how to help with that request. Please try rephrasing.",
                    new ArrayList<>(), true, new HashMap<>());
        }

        System.out.println("Supervisor selected " + queue.size() + " agents:");
        queue.forEach(
                node -> System.out.println("- " + node.getName() + ": " + node.getInstructions().get(0).getContent()));

        Map<String, List<Message>> agentOutputs = new HashMap<>();
        Set<String> finishedAgents = new HashSet<>();
        Set<String> processingAgents = new HashSet<>();

        agentOutputs.put("user", userInput);
        int maxIterations = queue.size() * 2, iterations = 0;

        while (!queue.isEmpty() && iterations++ < maxIterations) {
            AgentNode current = queue.poll();
            String agentName = current.getName();

            System.out.println("Processing agent: " + agentName + " (iteration " + iterations + ")");

            if (!processingAgents.add(agentName)) {
                System.err.println("Dependency cycle detected at: " + agentName);
                throw new IllegalStateException("Dependency cycle at: " + agentName);
            }

            if (areDependenciesMet(current, finishedAgents)) {
                List<Message> context = buildAgentContext(current, agentOutputs);
                System.out.println("\n\nHandoff to Agent: " + agentName + "\n\n");

                BaseAgent agent = agents.get(agentName);
                if (agent == null) {
                    System.err.println("Agent " + agentName + " not found in registered agents");
                    finishedAgents.add(agentName);
                    processingAgents.remove(agentName);
                    continue;
                }

                List<Message> response = agent.run(context);

                agentOutputs.put(agentName, response);
                finishedAgents.add(agentName);
                processingAgents.remove(agentName);

                // Check if this agent wants to become the handoff agent
                String agentFinalResponse = extractFinalResponse(response);
                SupervisorResponse agentParsedResponse = parseAgentResponse(agentFinalResponse);

                // If agent indicates it wants to take control (by not being ready to handoff
                // immediately)
                if (!agentParsedResponse.readyToHandoff && agentParsedResponse.content != null &&
                        !agentParsedResponse.content.trim().isEmpty()) {
                    System.out.println("Agent " + agentName + " requesting handoff control");
                    handoffAgent = agentName;
                    return agentParsedResponse;
                }
            } else {
                System.out.println("Agent " + agentName + " dependencies not met, re-queuing");
                queue.add(current);
            }
        }

        if (iterations >= maxIterations) {
            System.err.println("Exceeded max iterations (" + maxIterations + "), possible cycle detected");
            throw new IllegalStateException("Exceeded max iterations; possible cycle.");
        }

        System.out.println("All agents completed, summarizing results");
        String summary = summarizeResults(agentOutputs);
        return parseAgentResponse(summary);
    }

    private String extractFinalResponse(List<Message> messages) {
        // Get the last assistant message from the conversation
        for (int i = messages.size() - 1; i >= 0; i--) {
            Message msg = messages.get(i);
            if ("assistant".equals(msg.getRole()) && msg.getContent() != null) {
                return msg.getContent();
            }
        }
        return "No response generated";
    }

    private SupervisorResponse parseAgentResponse(String response) {
        try {
            // Try to parse as JSON first
            if (response.trim().startsWith("{") && response.trim().endsWith("}")) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode jsonNode = mapper.readTree(response);

                String content = jsonNode.has("summary") ? jsonNode.get("summary").asText()
                        : jsonNode.has("content") ? jsonNode.get("content").asText() : response;

                List<String> tags = new ArrayList<>();
                if (jsonNode.has("tags") && jsonNode.get("tags").isArray()) {
                    for (JsonNode tag : jsonNode.get("tags")) {
                        tags.add(tag.asText());
                    }
                }

                boolean readyToHandoff = jsonNode.has("readyToHandoff") ? jsonNode.get("readyToHandoff").asBoolean()
                        : false;

                Map<String, Object> data = new HashMap<>();
                if (jsonNode.has("data")) {
                    JsonNode dataNode = jsonNode.get("data");
                    data = mapper.convertValue(dataNode, new TypeReference<Map<String, Object>>() {});
                }

                return new SupervisorResponse(content, tags, readyToHandoff, data);
            }
        } catch (Exception e) {
            // Not JSON, treat as plain text
        }

        // Default response format for plain text
        return new SupervisorResponse(response, new ArrayList<>(), false, new HashMap<>());
    }

    // Legacy method for backward compatibility
    @Override
    public String execute(List<Message> userInput) {
        SupervisorResponse response = executeWithHandoff(userInput);
        return response.content;
    }

    // Public methods for handoff management
    public void setHandoffAgent(String agentName) {
        this.handoffAgent = agentName;
    }

    public String getHandoffAgent() {
        return this.handoffAgent;
    }

    public void clearHandoffAgent() {
        this.handoffAgent = null;
    }

    public boolean hasHandoffAgent() {
        return this.handoffAgent != null && !this.handoffAgent.trim().isEmpty();
    }

    // Public method to get an agent by name
    public BaseAgent getAgent(String name) {
        return agents.get(name);
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
        // DO NOT use GeneralPromptAppender here - it adds wrong JSON format for
        // interpreter
        return prompt.toString();
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

        System.out.println("Parsing LLM response for agent selection:");
        System.out.println("Response content: " + responseContent);

        try {
            // Clean the response content to handle markdown code blocks
            String cleanedContent = cleanJsonResponse(responseContent);
            System.out.println("Cleaned content: " + cleanedContent);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(cleanedContent);

            System.out.println("Parsed JSON node type: " + rootNode.getNodeType());

            // Handle both single object and array responses
            if (rootNode.isArray()) {
                // Response is an array of agent nodes
                System.out.println("Processing array of " + rootNode.size() + " agent nodes");
                for (JsonNode node : rootNode) {
                    AgentNode agentNode = parseAgentNode(node);
                    if (agentNode != null) {
                        agentNodes.add(agentNode);
                        System.out.println("Successfully parsed agent: " + agentNode.getName());
                    }
                }
            } else if (rootNode.isObject()) {
                // Response is a single agent node object
                System.out.println("Processing single agent node object");
                AgentNode agentNode = parseAgentNode(rootNode);
                if (agentNode != null) {
                    agentNodes.add(agentNode);
                    System.out.println("Successfully parsed agent: " + agentNode.getName());
                }
            } else {
                System.err.println("Invalid response format - not object or array: " + rootNode.getNodeType());
                throw new RuntimeException(
                        "Invalid response format: Expected JSON object or array, got: " + rootNode.getNodeType());
            }
        } catch (JsonProcessingException e) {
            System.err.println("JSON parsing failed: " + e.getMessage());
            System.err.println("Original response: " + responseContent);
            throw new RuntimeException("Failed to parse agent nodes: " + e.getMessage(), e);
        }

        System.out.println("Successfully parsed " + agentNodes.size() + " agent nodes");
        return agentNodes;
    }

    /**
     * Clean JSON response by removing markdown code blocks if present
     */
    private String cleanJsonResponse(String responseContent) {
        if (responseContent == null || responseContent.trim().isEmpty()) {
            return "[]"; // Return empty array as fallback
        }

        String cleaned = responseContent.trim();
        
        // Remove markdown code blocks if present
        if (cleaned.startsWith("```json") || cleaned.startsWith("```JSON")) {
            // Find the opening ```json or ```JSON
            int startIndex = cleaned.indexOf('\n');
            if (startIndex != -1) {
                cleaned = cleaned.substring(startIndex + 1);
            }
        } else if (cleaned.startsWith("```")) {
            // Handle generic ``` blocks
            int startIndex = cleaned.indexOf('\n');
            if (startIndex != -1) {
                cleaned = cleaned.substring(startIndex + 1);
            }
        }
        
        // Remove closing ``` if present
        if (cleaned.endsWith("```")) {
            int endIndex = cleaned.lastIndexOf("```");
            cleaned = cleaned.substring(0, endIndex);
        }
        
        return cleaned.trim();
    }

    private AgentNode parseAgentNode(JsonNode node) {
        System.out.println("Parsing individual agent node: " + node.toString());

        if (!node.has("name") || !node.has("instruction")) {
            System.err.println("Invalid agent node - missing required fields:");
            System.err.println("  - has 'name': " + node.has("name"));
            System.err.println("  - has 'instruction': " + node.has("instruction"));
            System.err.println("  - available fields: " + node.fieldNames().toString());
            System.err.println("  - node content: " + node.toString());
            return null;
        }

        String name = node.get("name").asText();
        String instruction = node.get("instruction").asText();

        System.out.println("Parsed agent name: " + name);
        System.out.println("Parsed instruction: " + instruction);

        HashSet<String> dependencies = new HashSet<>();
        JsonNode depsNode = node.get("dependency");
        if (depsNode != null && depsNode.isArray()) {
            for (JsonNode dep : depsNode) {
                dependencies.add(dep.asText());
            }
            System.out.println("Parsed dependencies: " + dependencies);
        } else {
            System.out.println("No dependencies found");
        }

        AgentNode agentNode = new AgentNode(name, dependencies, instruction);
        System.out.println("Successfully created AgentNode: " + name);
        return agentNode;
    }
}
