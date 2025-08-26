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

public class BaseSupervisor implements SupervisorInterface {
    private final Map<String, BaseAgent> agents = new HashMap<>();
    private String handoffAgent = null;
    private Queue<AgentNode> agentNodes = new LinkedList<>();
    private final Map<String, List<Message>> agentOutputs = new HashMap<>();
    private final Set<String> finishedAgents = new HashSet<>();
    private final Set<String> processingAgents = new HashSet<>();
    private int maxIterations = 0;
    private int iterations = 0;
    private String lastAgent = null;

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

    private static final String INTERPRETER_PROMPT = """
            SYSTEM
            You are Task-Orchestrator v2. Return ONLY a JSON array of agent nodes (see INTERFACE).

            AVAILABLE AGENTS
            - GoalCreatorAgent  ➜ Use **only** when the user explicitly asks to define/clarify a goal **or** provides no clear goal/task. Never include if the user directly requests a task.
            - TaskPlannerAgent ➜ Use **only** when either:
                1. The **latest** user message explicitly asks to plan or break down goals
                2. The message has HANDOFF_TAG and data.nextAgent = "TaskPlannerAgent"
                3. A goal has just been created and tasks need to be planned for it
            - TaskCreatorAgent ➜ Runs after TaskPlannerAgent to persist tasks exactly as planned. Always depends on TaskPlannerAgent; never runs alone and never depends on GoalCreatorAgent.
            - HabitAgent       ➜ Use when the user wants to create or manage a habit (type, cadence, verification, notifications) or log/complete habitual actions.

            HANDOFF MODE (STRICT)
            • If the latest message comes from the assistant and includes HANDOFF_TAG with data.nextAgent = "TaskPlannerAgent" OR data.lastAction = "goalCreated":
              - Return EXACTLY two agent nodes in this order with these dependencies:
                [
                  {"name":"TaskPlannerAgent","instruction":"Plan tasks for the newly created goal.","dependency":[]},
                  {"name":"TaskCreatorAgent","instruction":"Persist the tasks exactly as planned.","dependency":["TaskPlannerAgent"]}
                ]
              - Do NOT include GoalCreatorAgent.
              - Do NOT set TaskPlannerAgent to depend on GoalCreatorAgent.
              - Do NOT add any other agents.

            AGENT-SELECTION GUIDELINES
            1. If the message contains something like“set a goal”, “define my objective”, “I don’t know my goal”, etc. ➜ include GoalCreatorAgent.
            2. If the message references a specific goal or says “create a task”, “add a task to …”, skip GoalCreatorAgent.
            3. Include TaskPlannerAgent **only if** the most recent user utterance contains verbs like “create a task”, “plan tasks”, “schedule”, “add task”, etc. OR if the user just created a goal.
            4. **GOAL CREATION HANDOFF**: After a goal is successfully created, automatically include TaskPlannerAgent to plan tasks for the new goal.
            5. TaskCreatorAgent always depends on TaskPlannerAgent.
            6. Ensure a valid DAG—no circular dependencies.

            INTERFACE (must match exactly)
            [
            {
            "name": "AgentName",
            "instruction": "Specific instruction for this agent",
            "dependency": ["AgentName1", "AgentName2"]
            }
            ]

            INSTRUCTION INTELLIGENCE:
                - Be SPECIFIC about what the agent should accomplish
                - Include relevant context from the user's request
                - Reference specific goals, targets, or requirements mentioned
                - Don't just say "handle the user's request" - explain WHAT specifically to do
                - **EXAMPLE**: If user says "I want to run 50 miles in 1 year" → GoalCreatorAgent creates goal → TaskPlannerAgent plans tasks → TaskCreatorAgent persists tasks

            RULES
            • The JSON array cannot be empty.
            • Use only the four agent names above.
            • Output valid JSON—no extra text, comments, or explanations.
            • **GOAL FLOW**: GoalCreatorAgent → TaskPlannerAgent → TaskCreatorAgent (when creating new goals)

                """;

    @Override
    public void addAgent(String name, BaseAgent agent) {
        agents.put(name, agent);
    }

    // New response class for handoff mechanism
    public static class SupervisorResponse {
        public String content;
        public List<String> tags;
        public boolean readyToHandoff;
        public Map<String, Object> data;
        public boolean reInterpret;

        public SupervisorResponse(String content, List<String> tags, boolean readyToHandoff, Map<String, Object> data,
                boolean reInterpret) {
            this.content = content;
            this.tags = tags != null ? tags : new ArrayList<>();
            this.readyToHandoff = readyToHandoff;
            this.data = data;
            this.reInterpret = reInterpret;
        }
    }
    /*
     * 1) Checks for handoff agent, if yes goes to executeHandoffAgent
     * 2) add users input to agentOutputs (for context)
     * 3) interpret user input
     * 4) check to make sure agents were added to queue
     * 5) execute normal
     */
    public SupervisorResponse executeWithHandoff(List<Message> userInput) {
        // If there's an active handoff agent, execute it directly
        if (handoffAgent != null && agents.containsKey(handoffAgent)) {
            return executeHandoffAgent(userInput);
        }
        // Normal supervisor execution - initialize state
        this.agentOutputs.put("user", userInput);
        System.out.println("Supervisor determining which agents to use...");
        interpret(userInput);
        if (this.agentNodes.isEmpty()) {
            System.err.println("Failed to interpret user request - no agents selected");
            return new SupervisorResponse("I'm not sure how to help with that request. Please try rephrasing.",
                    new ArrayList<>(), true, new HashMap<>(), false);
        }
        System.out.println("Supervisor selected " + this.agentNodes.size() + " agents:");
        this.agentNodes.forEach(
                node -> System.out.println("- " + node.getName() + ": " + node.getInstructions().get(0).getMessage()));
        return executeNormal();
    }

    /*
     * 1) get handoff agent
     * 2) sets lastAgent to handoffAgent
     * 3) if agent is null we need to clear handoff and return to normal execution
     * 4) run agent with context including latest user's message
     * 5) extract response
     * 6) parse the response
     * 7) check if the agent is ready to handoff
     * 8) if ready to handoff, save the name of the agent
     * 9) set handoffAgent to null
     * 10) get the final message from the agent
     * 11) if reinterpret is true, executeWithHandoff with the final message
     * 12) save agent output for its name and final message <--- POTENTIALLY CHANGE TO USING THE CONTEXT AGENT FOR FINAL OUTPUT
     * 13) execute normal
     */
    private SupervisorResponse executeHandoffAgent(List<Message> context) {
        BaseAgent agent = agents.get(handoffAgent);
        this.lastAgent = handoffAgent;
        if (agent == null) {
            handoffAgent = null;
            return executeNormal();
        }
        try {
            List<Message> response = agent.run(context);
            String finalResponse = extractFinalResponse(response);
            SupervisorResponse parsedResponse = parseAgentResponse(finalResponse);
            if (parsedResponse.readyToHandoff) {
                String savedName = this.handoffAgent;
                handoffAgent = null;
                // get all of the messages from the agent + this block scoped context
                // create context summary with context agent
                // get summary message
                List<Message> finalMessage = List.of(response.get(response.size() - 1));
                if (parsedResponse.reInterpret) {
                    // Save the agent's summary + agent name on a stack for later processing
                    // append summary to finalMessage list
                    return executeWithHandoff(finalMessage);
                }
                // consider changing from finalmessage to context summary 
                saveAgentOutput(savedName, finalMessage);
                return executeNormal();
            }
            return parsedResponse;
        } catch (Exception e) {
            handoffAgent = null;
            return executeNormal();
        }
    }

    /*
     * Context: agentNodes is a queue of all the agents that need to be executed
     * 1) while there are agents in the queue, poll the first and set it to current
     * 2) set the lastAgent to the current agent's name
     * 3) check for a cycle in agent processing
     * 4) if all of the agents dependencies are met, build the context for the agent
     * --- Dependencies are met when all of the elements in the dependency list are in the finishedAgents set ---
     * 5) gets the instance of the current agent
     * 6) run the agent with the context
     * 7) parse the reponse to convert the JSON into a SupervisorResponse object
     * 8) check if the agent is ready to handoff
     * 9) if ready to handoff, set the handoffAgent to the agent's name
     * 10) return parsed response
     * 11) send the final response to the user
     */
    private SupervisorResponse executeNormal() {
        while (!this.agentNodes.isEmpty() && this.iterations++ < this.maxIterations) {
            AgentNode current = this.agentNodes.poll();
            String agentName = current.getName();
            this.lastAgent = agentName;
            if (!processingAgents.add(agentName)) {
                System.err.println("Dependency cycle detected at: " + agentName);
                throw new IllegalStateException("Dependency cycle at: " + agentName);
            }

            if (areDependenciesMet(current, finishedAgents)) {
                List<Message> context = buildAgentContext(current);
                BaseAgent agent = agents.get(agentName);
                List<Message> response = agent.run(context);
                SupervisorResponse agentParsedResponse = saveAgentOutput(agentName, response);
                    
                if (!agentParsedResponse.readyToHandoff && agentParsedResponse.content != null &&
                        !agentParsedResponse.content.trim().isEmpty()) {
                    handoffAgent = agentName;
                    return agentParsedResponse;
                }
            } else {
                this.agentNodes.add(current);
            }
        }

        if (iterations >= maxIterations) {
            throw new IllegalStateException("Exceeded max iterations; possible cycle.");
        }

        return parseAgentResponse(extractFinalResponse(this.agentOutputs.get(this.lastAgent)));
    }

    /*
     * 1) saves the agent's output to the agentOutputs map
     * 2) add the to finished set
     * 3) remove the agent from the processing set
     * 4) parse the response to convert the JSON into a SupervisorResponse object
     */
    private SupervisorResponse saveAgentOutput(String agentName, List<Message> response) {
        this.agentOutputs.put(agentName, response);
        finishedAgents.add(agentName);
        processingAgents.remove(agentName);
        return parseAgentResponse(extractFinalResponse(response));
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
                String content = jsonNode.has("content") ? jsonNode.get("content").asText() : response;
                // WIP: attempt to fix JSON string issue

                // String content;
                // if (jsonNode.has("content")) {
                // content = jsonNode.get("content").asText();
                // } else if (jsonNode.has("summary")) {
                // // Fallback to summary field if present
                // content = jsonNode.get("summary").asText();
                // } else {
                // content = response;
                // }
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
                    data = mapper.convertValue(dataNode, new TypeReference<Map<String, Object>>() {
                    });
                }
                boolean reInterpret = jsonNode.has("reInterpret") ? jsonNode.get("reInterpret").asBoolean() : false;
                return new SupervisorResponse(content, tags, readyToHandoff, data, reInterpret);
            }
        } catch (Exception e) {
            // Not JSON, treat as plain text
        }
        // Default response format for plain text
        return new SupervisorResponse(response, new ArrayList<>(), false, new HashMap<>(), false);
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
    public void interpret(List<Message> userInput) {
        // --- DEBUG LOG ----------------------------------------------------
        if (!userInput.isEmpty()) {
            Message lastMsg = userInput.get(userInput.size() - 1);
            System.out.printf("[INTERPRETER] Incoming user message (%s): %s%n", lastMsg.getRole(),
                    lastMsg.getContent());
        }

        this.iterations = 0;
        this.agentOutputs.clear();
        this.finishedAgents.clear();
        this.processingAgents.clear();
        List<Message> contexts = userInput;

        LlmResponse response = llmClient.generate(INTERPRETER_PROMPT, contexts);
        this.agentNodes = parseAgentNodes(response.getContent());
        this.maxIterations = this.agentNodes.size() * 2;
    }

    private boolean areDependenciesMet(AgentNode current, Set<String> finishedAgents) {
        return current.getDependencies().stream().allMatch(finishedAgents::contains);
    }

    /*
     * 1) Input is the current agent
     * 2) gets the instructions for the agent which is from interpreter
     * 3) get the outputs of all the agents in the dependency list
     * 4) if the dependency list is empty, add the user's input to the context
     * 5) return the context
     */
    private List<Message> buildAgentContext(AgentNode current) {
        // Null-safe creation of context - getInstructions() could return null
        List<Message> instructions = current.getInstructions();
        List<Message> context = instructions != null ? new ArrayList<>(instructions) : new ArrayList<>();
        for (String dep : current.getDependencies()) {
            context.addAll(this.agentOutputs.getOrDefault(dep, List.of()));
        }
        if (current.getDependencies().isEmpty() && this.agentOutputs.get("user") != null) {
            context.addAll(this.agentOutputs.get("user"));
        }
        return context;
    }

    private Queue<AgentNode> parseAgentNodes(String responseContent) {
        try {
            String cleanedContent = cleanJsonResponse(responseContent);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(cleanedContent);
            if (rootNode.isArray()) {
                for (JsonNode node : rootNode) {
                    AgentNode agentNode = parseAgentNode(node);
                    if (agentNode != null) {
                        this.agentNodes.add(agentNode);
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

    /**
     * Clean JSON response by removing markdown code blocks if present
     */
    private String cleanJsonResponse(String responseContent) {
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
    private AgentNode parseAgentNode(JsonNode node) {
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
        AgentNode agentNode = new AgentNode(name, dependencies, instruction);
        return agentNode;
    }
}