package com.optimal.backend.springboot.agent.framework.core.util;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;

import com.optimal.backend.springboot.agent.framework.core.AgentNode;

public class AgentDependencyGraph {

    private final Queue<AgentNode> agentNodes = new LinkedList<>();
    private final Set<String> finishedAgents = new HashSet<>();
    private final Set<String> processingAgents = new HashSet<>();
    private final Map<String, AgentNode> nodeMap = new HashMap<>();

    public AgentDependencyGraph(Queue<AgentNode> nodes) {
        this.agentNodes.addAll(nodes);
        for (AgentNode node : nodes) {
            nodeMap.put(node.getName(), node);
        }
        detectCycles();
    }

    /**
     * Detects cycles in the dependency graph using DFS.
     * Throws IllegalStateException if a cycle is found.
     */
    private void detectCycles() {
        Set<String> visited = new HashSet<>();
        Set<String> recursionStack = new HashSet<>();

        for (AgentNode node : nodeMap.values()) {
            if (hasCycle(node.getName(), visited, recursionStack)) {
                throw new IllegalStateException("Dependency cycle detected involving agent: " + node.getName());
            }
        }
    }

    private boolean hasCycle(String currentAgentName, Set<String> visited, Set<String> recursionStack) {
        if (recursionStack.contains(currentAgentName)) {
            return true;
        }
        if (visited.contains(currentAgentName)) {
            return false;
        }

        visited.add(currentAgentName);
        recursionStack.add(currentAgentName);

        AgentNode node = nodeMap.get(currentAgentName);
        if (node != null) {
            for (String dep : node.getDependencies()) {
                if (hasCycle(dep, visited, recursionStack)) {
                    return true;
                }
            }
        }

        recursionStack.remove(currentAgentName);
        return false;
    }

    public boolean isEmpty() {
        return agentNodes.isEmpty();
    }

    public boolean isFinished(String agentName) {
        return finishedAgents.contains(agentName);
    }

    public void markFinished(String agentName) {
        finishedAgents.add(agentName);
        processingAgents.remove(agentName);
    }

    /**
     * Gets the next runnable agent whose dependencies are met.
     * Requeues agents whose dependencies are not met.
     * 
     * @return The next executable AgentNode, or null if no agents are currently
     *         runnable.
     */
    public AgentNode getNextRunnableAgent() {
        int initialSize = agentNodes.size();
        for (int i = 0; i < initialSize; i++) {
            AgentNode current = agentNodes.poll();
            if (current == null) {
                return null;
            }

            String agentName = current.getName();

            // Detect immediate self-loops or duplicate processing attempts in a single pass
            // if needed,
            // though strict cycle detection is handled at init.
            if (processingAgents.contains(agentName)) {
                // Should ideally not happen if logic is correct, but safe to requeue or skip
                agentNodes.add(current);
                continue;
            }

            if (areDependenciesMet(current)) {
                processingAgents.add(agentName);
                return current;
            } else {
                agentNodes.add(current);
            }
        }
        return null;
    }

    public void addNode(AgentNode node) {
        agentNodes.add(node);
        nodeMap.put(node.getName(), node);
    }

    private boolean areDependenciesMet(AgentNode current) {
        return current.getDependencies().stream().allMatch(finishedAgents::contains);
    }

    public Set<String> getFinishedAgents() {
        return new HashSet<>(finishedAgents);
    }

    public int size() {
        return agentNodes.size();
    }

    public List<String> getPendingAgentNames() {
        return agentNodes.stream().map(AgentNode::getName).toList();
    }
}
