package com.optimal.backend.springboot.agent.framework.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.springframework.beans.factory.annotation.Autowired;

import com.optimal.backend.springboot.agent.framework.core.config.SupervisorPrompts;
import com.optimal.backend.springboot.agent.framework.core.interfaces.SupervisorInterface;
import com.optimal.backend.springboot.agent.framework.core.model.SupervisorResponse;
import com.optimal.backend.springboot.agent.framework.core.util.AgentDependencyGraph;
import com.optimal.backend.springboot.agent.framework.core.util.SupervisorJsonParser;
import com.optimal.backend.springboot.service.ChatService;

public class BaseSupervisor implements SupervisorInterface {
    private final Map<String, BaseAgent> agents = new HashMap<>();
    private String handoffAgent = null;
    private AgentDependencyGraph dependencyGraph;

    private final Map<String, List<Message>> agentOutputs = new HashMap<>();
    private final Map<String, List<Message>> agentContexts = new HashMap<>();

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
    }

    public BaseSupervisor() {
        // LlmClient will be injected by Spring via @Autowired
    }

    @Override
    public void addAgent(String name, BaseAgent agent) {
        agents.put(name, agent);
    }

    public SupervisorResponse executeWithHandoff(List<Message> userInput) {
        System.out.println(
                "\n\n====================================================================================================");

        // Loop to handle re-interpretation without recursion (Fix FLAW 3)
        List<Message> currentInput = userInput;
        boolean keepRunning = true;

        while (keepRunning) {
            // 1. Handle Active Handoff
            if (handoffAgent != null && agents.containsKey(handoffAgent)) {
                System.out.print("\n\nExecuting With Handoff - " + handoffAgent + " still in control\n");
                SupervisorResponse response = executeHandoffAgent(currentInput);

                if (response.reInterpret) {
                    System.out.println("Executing Re-interpret loop...");
                    // Prepare context for next iteration, update currentInput if needed
                    // In current logic, re-interpret usually implies a fresh start with new context
                    // We need to ensure we have the latest message from the handoff agent
                    if (this.agentOutputs.containsKey(lastAgent)) {
                        List<Message> lastOutput = this.agentOutputs.get(lastAgent);
                        if (!lastOutput.isEmpty()) {
                            currentInput = new ArrayList<>(userInput);
                            currentInput.add(lastOutput.get(lastOutput.size() - 1));
                        }
                    }
                    continue; // Loop back to start
                } else if (response.readyToHandoff) {
                    // Handoff is done, handoffAgent is already cleared in executeHandoffAgent
                    System.out.println("Handoff complete, resuming normal execution...");
                    // Fall through to normal execution
                } else {
                    // Still in handoff mode or finished with a response
                    return response;
                }
            }

            // 2. Normal Execution
            this.agentOutputs.put("user", currentInput);
            System.out.println("Supervisor determining which agents to use...");
            interpret(currentInput);

            if (dependencyGraph == null || dependencyGraph.isEmpty()) {
                System.err.println("Failed to interpret user request - no agents selected");
                return new SupervisorResponse("I'm not sure how to help with that request. Please try rephrasing.",
                        new ArrayList<>(), true, new HashMap<>(), false, -1);
            }

            System.out.println("Supervisor selected " + dependencyGraph.size() + " agents:");
            // We can't iterate easily to print instructions without exposing internal
            // queue,
            // but we can trust the graph is built.

            SupervisorResponse normalResponse = executeNormal();

            // Check if normal execution triggered a handoff that needs immediate attention?
            // The current contract implies executeNormal runs until completion or handoff.
            // If it returns with readyToHandoff=false, it means we are done.
            return normalResponse;
        }

        return new SupervisorResponse("Execution terminated unexpectedly.", new ArrayList<>(), false, new HashMap<>(),
                false, -1);
    }

    private SupervisorResponse executeHandoffAgent(List<Message> context) {
        BaseAgent agent = agents.get(handoffAgent);
        this.lastAgent = this.handoffAgent;

        if (agent == null) {
            System.out.println("Handoff agent does not exist");
            this.handoffAgent = null;
            // Return a dummy response to signal fallback to normal flow
            return new SupervisorResponse("", null, true, null, false, -1);
        }

        try {
            List<Message> response = agent.run(context);
            String finalResponse = SupervisorJsonParser.extractFinalResponse(response);
            int tokens = response.get(response.size() - 1).getTokens();
            SupervisorResponse parsedResponse = SupervisorJsonParser.parseAgentResponse(finalResponse);

            System.out.println("Handoff = " + parsedResponse.readyToHandoff);
            System.out.println("ReInterpret = " + parsedResponse.reInterpret);

            if (parsedResponse.readyToHandoff) {
                String savedName = this.handoffAgent;
                this.handoffAgent = null;
                List<Message> finalMessage = List.of(response.get(response.size() - 1));

                if (parsedResponse.reInterpret) {
                    agentContexts.put(savedName, finalMessage);
                    if (chatService != null) {
                        chatService.addAgentMessage(UserContext.getChatId(), UserContext.getUserId(),
                                parsedResponse.content, tokens);
                    }
                    // Return with reInterpret flag true so main loop handles it
                    return parsedResponse;
                }

                System.out.println("Executing Normal (post-handoff)");
                saveAgentOutput(savedName, response);
                // Return with readyToHandoff true so main loop falls through to normal
                // execution
                return parsedResponse;
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
            return new SupervisorResponse("Error in handoff agent: " + e.getMessage(), null, true, null, false, -1);
        }
    }

    private SupervisorResponse executeNormal() {
        int tokens = 0;
        int maxIterations = dependencyGraph.size() * 2; // Safety break
        int iterations = 0;

        while (!dependencyGraph.isEmpty() && iterations++ < maxIterations) {
            AgentNode current = dependencyGraph.getNextRunnableAgent();

            if (current == null) {
                // No runnable agents found, but graph not empty -> Cycle or deadlock
                // (Cycle should be caught at init, so this is likely a logic error or complex
                // dependency state)
                throw new IllegalStateException("Deadlock detected: agents remaining but none runnable.");
            }

            String agentName = current.getName();
            this.lastAgent = agentName;

            Message context = buildAgentContext(current);
            List<Message> structuredContext = new ArrayList<>();
            structuredContext.add(context);

            BaseAgent agent = agents.get(agentName);

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
        }

        if (iterations >= maxIterations) {
            // Should not happen with DAG check
            throw new IllegalStateException("Exceeded max iterations.");
        }

        if (chatService != null) {
            chatService.addAgentMessage(UserContext.getChatId(), UserContext.getUserId(),
                    SupervisorJsonParser.extractFinalResponse(this.agentOutputs.get(this.lastAgent)), tokens);
        }
        return SupervisorJsonParser
                .parseAgentResponse(SupervisorJsonParser.extractFinalResponse(this.agentOutputs.get(this.lastAgent)));
    }

    private SupervisorResponse saveAgentOutput(String agentName, List<Message> response) {
        this.agentOutputs.put(agentName, response);
        dependencyGraph.markFinished(agentName);
        return SupervisorJsonParser.parseAgentResponse(SupervisorJsonParser.extractFinalResponse(response));
    }

    public void setHandoffAgent(String agentName) {
        this.handoffAgent = agentName;
    }

    public String getHandoffAgent() {
        return this.handoffAgent;
    }

    public void clearHandoffAgent() {
        this.handoffAgent = null;
    }

    public void clearAllState() {
        this.handoffAgent = null;
        // Re-initialize empty graph
        this.dependencyGraph = new AgentDependencyGraph(new java.util.LinkedList<>());
        this.agentOutputs.clear();
        this.agentContexts.clear();
        this.lastAgent = null;
        System.out.println("[SUPERVISOR] Cleared all internal state for memory cleanup");
    }

    public boolean isProcessingComplete() {
        return (dependencyGraph == null || dependencyGraph.isEmpty()) && this.handoffAgent == null;
    }

    public boolean hasHandoffAgent() {
        return this.handoffAgent != null && !this.handoffAgent.trim().isEmpty();
    }

    public BaseAgent getAgent(String name) {
        return agents.get(name);
    }

    // Evaluation helper methods
    public List<String> getSelectedAgentNames() {

        return dependencyGraph != null ? dependencyGraph.getPendingAgentNames() : new ArrayList<>();
    }

    public String getLastExecutedAgent() {
        return lastAgent;
    }

    public String getHandoffAgentName() {
        return handoffAgent;
    }

    @Override
    public void interpret(List<Message> userInput) {
        if (!userInput.isEmpty()) {
            Message lastMsg = userInput.get(userInput.size() - 1);
            System.out.printf("\n[INTERPRETER] Incoming user message (%s): %s%n", lastMsg.getRole(),
                    lastMsg.getTextContent());
        }

        this.agentOutputs.clear();

        List<Message> contexts = userInput;
        LlmResponse response = llmClient.generate(SupervisorPrompts.INTERPRETER_PROMPT, contexts, "light");

        Queue<AgentNode> nodes = SupervisorJsonParser.parseAgentNodes(response.getContent());
        this.dependencyGraph = new AgentDependencyGraph(nodes); // Fix FLAW 2: Cycles detected here
    }

    @Override
    public String execute(List<Message> userInput) {
        SupervisorResponse response = executeWithHandoff(userInput);
        return response.content;
    }

    private Message buildAgentContext(AgentNode current) {
        List<Message> context = new ArrayList<>();
        context.addAll(agentContexts.getOrDefault(current.getName(), List.of()));
        agentContexts.remove(current.getName());
        for (String dep : current.getDependencies()) {
            context.addAll(this.agentOutputs.getOrDefault(dep, List.of()));
        }
        if (current.getDependencies().isEmpty() && this.agentOutputs.get("user") != null) {
            context.addAll(this.agentOutputs.get("user"));
        }

        // Return the last message as context, or instruction if empty
        if (!context.isEmpty()) {
            return context.get(context.size() - 1);
        } else {
            return current.getInstructions().get(0);
        }
    }
}
