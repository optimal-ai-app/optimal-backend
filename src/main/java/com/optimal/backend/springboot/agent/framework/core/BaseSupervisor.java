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
import com.optimal.backend.springboot.service.ChatService;

public class BaseSupervisor implements SupervisorInterface {
    private final Map<String, BaseAgent> agents = new HashMap<>();
    private String handoffAgent = null;
    private Queue<AgentNode> agentNodes = new LinkedList<>();
    private final Map<String, List<Message>> agentOutputs = new HashMap<>();
    private final Map<String, List<Message>> agentContexts = new HashMap<>();
    private final Set<String> finishedAgents = new HashSet<>();
    private final Set<String> processingAgents = new HashSet<>();
    private int maxIterations = 0;
    private int iterations = 0;
    private String lastAgent = null;

    @Autowired
    private LlmClient llmClient;
    @Autowired
    private ChatService chatService;

    // Constructor for manual dependency injection
    public BaseSupervisor(LlmClient llmClient) {
        this.llmClient = llmClient;
    }

    public void setLlmClient(LlmClient llmClient) {
        this.llmClient = llmClient;
    }

    public void setChatService(ChatService chatService) {
        this.chatService = chatService;
    } // Default constructor for Spring dependency injection

    public BaseSupervisor() {
        // LlmClient will be injected by Spring via @Autowired
    }

    private static final String INTERPRETER_PROMPT = """
              SYSTEM
              You are Task-Orchestrator. Return ONLY a JSON array of agent nodes (see INTERFACE).
              You never assemble ad-hoc mixes. You select from predefined teams, but you OUTPUT ONLY AGENTS in the shown format.


              PREDEFINED TEAMS
                  GoalDefinitionTeam → [GoalCreatorAgent]
                  MilestoneExecutionTeam → [MilestonePlannerAgent, MilestoneTaskCreatorAgent]
                  • MilestoneTaskCreatorAgent depends on MilestonePlannerAgent
                  TaskExecutionTeam → [TaskPlannerAgent, TaskCreatorAgent]
                  • TaskCreatorAgent depends on TaskPlannerAgent


             TEAM SELECTION
             If the user says "goal" or "plan a goal" or "create a goal" → select GoalDefinitionTeam.
             If the user mentions "milestones" or "milestone" for some specific goal OR a goal has just been added → select MilestoneExecutionTeam.
             If the user asks to "create tasks", "plan tasks" and "schedule tasks" → select TaskExecutionTeam.

             IMPORTANT RULES:
             - If the message indicates a goal was ALREADY created (e.g., "We've added your goal", "goal has been created"), do NOT select GoalDefinitionTeam.
             - If the message says "let's create milestones" or "come up with milestones", select ONLY MilestoneExecutionTeam.
             - If the message indicates milestones were just created (ex: "I have generated [number] milestones for [goal name]. Let's create some tasks for these milestones!"), select ONLY TaskExecutionTeam.


             OUTPUT CONSTRUCTION
             **CRITICAL**: When you select a team, you MUST output ALL agents from that team.
             - MilestoneExecutionTeam → output BOTH MilestonePlannerAgent AND MilestoneTaskCreatorAgent (2 agents)
             - TaskExecutionTeam → output BOTH TaskPlannerAgent AND TaskCreatorAgent (2 agents)
             - GoalDefinitionTeam → output GoalCreatorAgent (1 agent)

             Output all agents from the selected team as an array of agent nodes per INTERFACE.
             Ensure valid dependencies:
                 • MilestoneTaskCreatorAgent must depend on MilestonePlannerAgent when both are present
                 • TaskCreatorAgent must depend on TaskPlannerAgent when both are present
                 • GoalCreatorAgent and MilestonePlannerAgent have no dependencies (empty array: []).
             Do not include team names in the output.


              INSTRUCTION INTELLIGENCE
              Make each agent's "instruction" specific to the user's context, constraints, timelines, and success metrics.
              Reference explicit goals, milestones, or requirements if given.
              CRITICAL: When MilestoneTaskCreatorAgent depends on MilestonePlannerAgent, instruction must say "Create MILESTONE tasks for goal [name]"
              CRITICAL: When TaskCreatorAgent depends on TaskPlannerAgent, instruction must say "Create REGULAR tasks for milestone [name] of goal [goal name]"


             OUTPUT (must match exactly)
             [
                 {
                     "name": "AgentName",
                     "instruction": "Specific instruction for this agent",
                     "dependency": ["AgentName1", "AgentName2"]
                 }
             ]


             EXAMPLE OUTPUT FOR GoalDefinitionTeam:
             [
                 {
                     "name": "GoalCreatorAgent",
                     "instruction": "Create a goal to [specific goal description] with [timeline/constraints]",
                     "dependency": []
                 }
             ]


             EXAMPLE OUTPUT FOR MilestoneExecutionTeam:
             [
                 {
                     "name": "MilestonePlannerAgent",
                     "instruction": "Plan milestones to achieve the goal of [goal name]",
                     "dependency": []
                 },
                 {
                     "name": "MilestoneTaskCreatorAgent",
                     "instruction": "Create MILESTONE tasks for goal [goal name]",
                     "dependency": ["MilestonePlannerAgent"]
                 }
             ]


             EXAMPLE OUTPUT FOR TaskExecutionTeam:
             [
                 {
                     "name": "TaskPlannerAgent",
                     "instruction": "Plan tasks for milestone [milestone name] of goal [goal name]",
                     "dependency": []
                 },
                 {
                     "name": "TaskCreatorAgent",
                     "instruction": "Create REGULAR tasks for milestone [milestone name] of goal [goal name]",
                     "dependency": ["TaskPlannerAgent"]
                 }
             ]


             RULES
            • The JSON array cannot be empty.
            • Use only the six allowed agent names: GoalCreatorAgent, MilestonePlannerAgent, MilestoneTaskCreatorAgent, TaskPlannerAgent, TaskCreatorAgent.
            • Output valid JSON—no extra text, comments, or explanations.
            • When a team has multiple agents, ALL agents must be in the output array.
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
        public int currentStep;

        public SupervisorResponse(String content, List<String> tags, boolean readyToHandoff, Map<String, Object> data,
                boolean reInterpret, int currentStep) {
            this.content = content;
            this.tags = tags != null ? tags : new ArrayList<>();
            this.readyToHandoff = readyToHandoff;
            this.data = data;
            this.reInterpret = reInterpret;
            this.currentStep = currentStep;
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
        System.out.println(
                "\n\n====================================================================================================");
        System.out.print("\n\nExecuting With Handoff - ");
        if (handoffAgent != null && agents.containsKey(handoffAgent)) {
            System.out.println(handoffAgent + " still in control\n");
            return executeHandoffAgent(userInput);
        }
        // Normal supervisor execution - initialize state
        this.agentOutputs.put("user", userInput);
        System.out.println("Supervisor determining which agents to use...");
        interpret(userInput);
        if (this.agentNodes.isEmpty()) {
            System.err.println("Failed to interpret user request - no agents selected");
            return new SupervisorResponse("I'm not sure how to help with that request. Please try rephrasing.",
                    new ArrayList<>(), true, new HashMap<>(), false, -1);
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
     * 12) save agent output for its name and final message <--- POTENTIALLY CHANGE
     * TO USING THE CONTEXT AGENT FOR FINAL OUTPUT
     * 13) execute normal
     */
    private SupervisorResponse executeHandoffAgent(List<Message> context) {
        BaseAgent agent = agents.get(handoffAgent);
        this.lastAgent = this.handoffAgent;
        if (agent == null) {
            System.out.println("Handoffagent does not exist");
            this.handoffAgent = null;
            return executeNormal();
        }
        try {
            List<Message> response = agent.run(context);
            String finalResponse = extractFinalResponse(response);
            int tokens = response.get(response.size() - 1).getTokens();
            SupervisorResponse parsedResponse = parseAgentResponse(finalResponse);
            System.out.println("Handoff = " + parsedResponse.readyToHandoff);
            System.out.println("ReInterpret = " + parsedResponse.reInterpret);
            if (parsedResponse.readyToHandoff == true) {
                String savedName = this.handoffAgent;
                this.handoffAgent = null;
                List<Message> fullContent = new ArrayList<>(context);
                fullContent.addAll(response);
                List<Message> finalMessage = List.of(response.get(response.size() - 1));
                if (parsedResponse.reInterpret == true) {
                    System.out.println("Executing Re-interpret");
                    // List<Message> summary = contextAgent.run(fullContent);
                    // System.out.println("\nSummary: " + summary.get(0).getContent());
                    agentContexts.put(savedName, finalMessage);
                    if (chatService != null) {
                        chatService.addAgentMessage(UserContext.getChatId(), UserContext.getUserId(),
                                parsedResponse.content, tokens);
                    }
                    return executeWithHandoff(finalMessage);
                }
                System.out.println("Executing Normal");
                // consider changing from finalmessage to context summary
                saveAgentOutput(savedName, finalMessage);
                return executeNormal();
            }
            if (parsedResponse.currentStep > 0) {
                agent.updateFlowStep(parsedResponse.currentStep);
            }
            if (chatService != null) {
                chatService.addAgentMessage(UserContext.getChatId(), UserContext.getUserId(), parsedResponse.content,
                        tokens);
            }
            return parsedResponse;
        } catch (Exception e) {
            this.handoffAgent = null;
            return executeNormal();
        }
    }

    /*
     * Context: agentNodes is a queue of all the agents that need to be executed
     * 1) while there are agents in the queue, poll the first and set it to current
     * 2) set the lastAgent to the current agent's name
     * 3) check for a cycle in agent processing
     * 4) if all of the agents dependencies are met, build the context for the agent
     * --- Dependencies are met when all of the elements in the dependency list are
     * in the finishedAgents set ---
     * 5) gets the instance of the current agent
     * 6) run the agent with the context
     * 7) parse the reponse to convert the JSON into a SupervisorResponse object
     * 8) check if the agent is ready to handoff
     * 9) if ready to handoff, set the handoffAgent to the agent's name
     * 10) return parsed response
     * 11) send the final response to the user
     */
    private SupervisorResponse executeNormal() {
        int tokens = 0;
        while (!this.agentNodes.isEmpty() && this.iterations++ < this.maxIterations) {
            AgentNode current = this.agentNodes.poll();
            String agentName = current.getName();
            this.lastAgent = agentName;
            if (!processingAgents.add(agentName)) {
                System.err.println("Dependency cycle detected at: " + agentName);
                throw new IllegalStateException("Dependency cycle at: " + agentName);
            }

            if (areDependenciesMet(current, finishedAgents)) {

                Message context = buildAgentContext(current);
                List<Message> structuredContext = new ArrayList<Message>();
                structuredContext.add(context);

                BaseAgent agent = agents.get(agentName);
                agent.systemPrompt = context.getTextContent() + agent.systemPrompt;

                List<Message> response = agent.run(structuredContext);
                tokens = response.get(response.size() - 1).getTokens();
                SupervisorResponse agentParsedResponse = saveAgentOutput(agentName, response);
                agent.currentFlowStep = agentParsedResponse.currentStep;
                if (!agentParsedResponse.readyToHandoff && agentParsedResponse.content != null &&
                        !agentParsedResponse.content.trim().isEmpty()) {
                    handoffAgent = agentName;
                    if (chatService != null) {
                        chatService.addAgentMessage(UserContext.getChatId(), UserContext.getUserId(),
                                agentParsedResponse.content, tokens);
                    }
                    return agentParsedResponse;
                }
            } else {
                this.agentNodes.add(current);
            }
        }

        if (iterations >= maxIterations) {
            throw new IllegalStateException("Exceeded max iterations; possible cycle.");
        }
        if (chatService != null) {
            chatService.addAgentMessage(UserContext.getChatId(), UserContext.getUserId(),
                    extractFinalResponse(this.agentOutputs.get(this.lastAgent)), tokens);
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

    /**
     * Clear all internal state for memory cleanup - call when supervisor will no
     * longer be used
     */
    public void clearAllState() {
        this.handoffAgent = null;
        this.agentNodes.clear();
        this.agentOutputs.clear();
        this.agentContexts.clear();
        this.finishedAgents.clear();
        this.processingAgents.clear();
        this.iterations = 0;
        this.maxIterations = 0;
        this.lastAgent = null;
        System.out.println("[SUPERVISOR] Cleared all internal state for memory cleanup");
    }

    /**
     * Check if all agent processing is complete (agentNodes queue is empty and no
     * handoff agent)
     */
    public boolean isProcessingComplete() {
        return this.agentNodes.isEmpty() && this.handoffAgent == null;
    }

    public boolean hasHandoffAgent() {
        return this.handoffAgent != null && !this.handoffAgent.trim().isEmpty();
    }

    // Public method to get an agent by name
    public BaseAgent getAgent(String name) {
        return agents.get(name);
    }

    // Evaluation helper methods
    public List<String> getSelectedAgentNames() {
        List<String> agentNames = new ArrayList<>();
        Queue<AgentNode> nodesCopy = new LinkedList<>(agentNodes);
        while (!nodesCopy.isEmpty()) {
            agentNames.add(nodesCopy.poll().getName());
        }
        return agentNames;
    }

    public String getLastExecutedAgent() {
        return lastAgent;
    }

    public String getHandoffAgentName() {
        return handoffAgent;
    }

    @Override
    public void interpret(List<Message> userInput) {
        // --- DEBUG LOG ----------------------------------------------------
        if (!userInput.isEmpty()) {
            Message lastMsg = userInput.get(userInput.size() - 1);
            System.out.printf("\n[INTERPRETER] Incoming user message (%s): %s%n", lastMsg.getRole(),
                    lastMsg.getTextContent());
        }

        this.iterations = 0;
        this.agentOutputs.clear();
        this.finishedAgents.clear();
        this.processingAgents.clear();
        List<Message> contexts = userInput;
        LlmResponse response = llmClient.generate(INTERPRETER_PROMPT, contexts, "light");
        this.agentNodes = parseAgentNodes(response.getContent());
        this.maxIterations = this.agentNodes.size() * 2;
    }

    private boolean areDependenciesMet(AgentNode current, Set<String> finishedAgents) {
        return current.getDependencies().stream().allMatch(finishedAgents::contains);
    }

    /*
     * 1) Input is the current agent
     * 2) gets the instructions for the agent which is from interpreter
     * 3) if the agent has a context already saved, add it to the context
     * 4) remove the agent's context from the map
     * 5) get the outputs of all the agents in the dependency list
     * 6) if the dependency list is empty, add the user's input to the context
     * 7) return the context
     */
    private Message buildAgentContext(AgentNode current) {
        // Null-safe creation of context - getInstructions() could return null
        List<Message> context = new ArrayList<>();
        context.addAll(agentContexts.getOrDefault(current.getName(), List.of()));
        agentContexts.remove(current.getName());
        for (String dep : current.getDependencies()) {
            context.addAll(this.agentOutputs.getOrDefault(dep, List.of()));
        }
        if (current.getDependencies().isEmpty() && this.agentOutputs.get("user") != null) {
            context.addAll(this.agentOutputs.get("user"));
        }
        System.out.println("====== BUILDING AGENT CONTEXT ======");
        for (Message m : context) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                System.out.println("\n" + mapper.writeValueAsString(m));
            } catch (Exception e) {
                System.out.println();
            }
        }
        try {
            int lastIndex = context.size() - 1;
            return context.get(lastIndex);
        } catch (Exception e) {
            List<Message> instructions = current.getInstructions();
            return instructions.get(0);
        }
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
