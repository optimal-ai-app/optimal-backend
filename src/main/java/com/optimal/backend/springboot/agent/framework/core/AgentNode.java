package com.optimal.backend.springboot.agent.framework.core;

import java.util.ArrayList;
import java.util.HashSet;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AgentNode {
    private String name;
    private HashSet<String> dependencies;
    private ArrayList<Message> instructions;

    public AgentNode(String name, HashSet<String> dependencies, String instruction) {
        this.name = name;
        this.dependencies = new HashSet<>(dependencies);
        this.instructions = new ArrayList<>();
        this.instructions.add(new Message("user", instruction));
        System.out.println("\n\n--------------------------------");
        System.out.println("AgentNode created: " + name);
        System.out.println("Instructions: " + instructions.get(0).getContent());
        System.out.println("Dependencies: " + dependencies);
        System.out.println("--------------------------------\n\n");
    }

    public void addInstruction(Message instruction) {
        instructions.add(instruction);
    }

    public void addDependency(String dependency) {
        dependencies.add(dependency);
    }

    public void removeDependency(String dependency) {
        dependencies.remove(dependency);
    }
}
