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
import com.optimal.backend.springboot.agent.framework.core.system.GeneralPromptAppender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BaseSupervisor implements SupervisorInterface {
    private final Map<String, BaseAgent> agents = new HashMap<>();

    @Autowired
    private LlmClient llmClient;

    // Base system prompt template for interpreting user input - agents will be
    // dynamically appended
    private static final String INTERPRETER_SYSTEM_PROMPT_BASE = """
            You are an intelligent task coordinator that analyzes user requests and determines which agents should execute to fulfill the request.

            Your job is to:
            1. Analyze the user's input to understand what they want to accomplish
            2. Determine which agents are needed to complete the task
            3. Identify any dependencies between agents (which agents must finish before others can start)
            4. Provide specific instructions for each agent

            """;

    private static final String INTERPRETER_SYSTEM_PROMPT_INSTRUCTIONS = """

            You MUST respond with a valid JSON array of agent nodes. Each agent node must have this exact structure:
            {
                "name": "AgentName",
                "instruction": "Specific instruction for this agent",
                "dependency": ["AgentName1", "AgentName2"]
            }

            Rules:
            - "name" must be exactly one of the available agent names listed above
            - "instruction" should be a clear, specific task for that agent
            - "dependency" is an array of agent names that must complete before this agent can start (empty array [] if no dependencies)
            - The dependency system creates a Directed Acyclic Graph (DAG) - agents with no dependencies run first, then agents whose dependencies are complete
            - DO NOT create circular dependencies
            - If the user's request doesn't require any agents, return an empty array []
            - ONLY use agent names that exist in the available agents list above

            Example responses:
            For "Add a goal to learn Spanish and create 3 todos for it":
            [
                {
                    "name": "GoalAgent",
                    "instruction": "Add a new goal: Learn Spanish",
                    "dependency": []
                },
                {
                    "name": "TodoAgent",
                    "instruction": "Create 3 todo items to help achieve the Spanish learning goal",
                    "dependency": ["GoalAgent"]
                }
            ]

            For "Schedule a meeting tomorrow at 2pm":
            [
                {
                    "name": "ScheduleAgent",
                    "instruction": "Schedule a meeting for tomorrow at 2:00 PM",
                    "dependency": []
                }
            ]

            Always respond with valid JSON only - no additional text or explanation.
            """;

    // System prompt for summarizing the execution results
    private static final String SUMMARIZER_SYSTEM_PROMPT = """
            You are a concise summarizer that reviews the entire conversation between the user and various agents, then provides a brief, clear summary of what was accomplished.

            Your task:
            1. Review all the messages in the conversation context
            2. Identify what actions were taken by each agent
            3. Provide a concise, user-friendly summary of the results
            4. Focus on concrete outcomes and deliverables

            Guidelines:
            - Use first person ("I") when describing actions taken on behalf of the user
            - Be specific about what was created, updated, or completed
            - Mention quantities (e.g., "I created 5 todo items", "I scheduled 2 meetings")
            - Keep it conversational and friendly
            - If something failed or had errors, mention it briefly but focus on what succeeded
            - Aim for 1-3 sentences maximum

            Example summaries:
            - "I added your goal 'Learn Spanish' to your goal list and created 3 todo items to help you get started."
            - "I scheduled your meeting for tomorrow at 2:00 PM and set up a reminder notification for 1 hour before."
            - "I generated a weekly productivity report showing you completed 8 out of 10 planned tasks this week."
            - "I updated your 'Exercise Daily' goal to mark it as completed and removed the associated todo items."

            Focus on outcomes that matter to the user, not internal processing steps.
            """;

    /**
     * Builds the interpreter system prompt dynamically based on registered agents
     */
    private String buildInterpreterSystemPrompt() {
        StringBuilder prompt = new StringBuilder(INTERPRETER_SYSTEM_PROMPT_BASE);

        // Add available agents section
        prompt.append("Available agents and their capabilities:\n");

        if (agents.isEmpty()) {
            prompt.append("- No agents are currently registered\n");
        } else {
            for (Map.Entry<String, BaseAgent> entry : agents.entrySet()) {
                String agentName = entry.getKey();
                BaseAgent agent = entry.getValue();
                String description = agent.getDescription();

                // Use agent name from the key, and description from the agent
                // If description is null or empty, provide a generic description
                if (description == null || description.trim().isEmpty()) {
                    description = "Agent for handling " + agentName.toLowerCase() + " related tasks";
                }

                prompt.append("- ").append(agentName).append(": ").append(description).append("\n");
            }
        }

        // Add the instructions part
        prompt.append(INTERPRETER_SYSTEM_PROMPT_INSTRUCTIONS);

        // Append general instructions to ensure consistent behavior
        return GeneralPromptAppender.appendGeneralInstructions(prompt.toString());
    }

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
        List<Message> contexts = new ArrayList<>();
        contexts.add(new Message("user", userInput));

        // Build the dynamic system prompt based on registered agents
        String dynamicSystemPrompt = buildInterpreterSystemPrompt();

        LlmResponse response = llmClient.generate(dynamicSystemPrompt, contexts, new ArrayList<>());
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
    public String summarize(List<Message> contexts) {
        List<Message> summaryContexts = new ArrayList<>(contexts);

        // Append general instructions to the summarizer prompt as well
        String enhancedSummarizerPrompt = GeneralPromptAppender.appendGeneralInstructions(SUMMARIZER_SYSTEM_PROMPT);

        LlmResponse response = llmClient.generate(enhancedSummarizerPrompt, summaryContexts, new ArrayList<>());
        return response.getContent();
    }
}
