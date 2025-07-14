package com.optimal.backend.springboot.agent.framework.core.interfaces;

import java.util.List;
import java.util.Queue;

import com.optimal.backend.springboot.agent.framework.core.AgentNode;
import com.optimal.backend.springboot.agent.framework.core.BaseAgent;
import com.optimal.backend.springboot.agent.framework.core.Message;

public interface SupervisorInterface {
    void addAgent(String name, BaseAgent agent);

    String execute(List<Message> userInput);

    void interpret(List<Message> userInput);

    // String summarize(List<Message> contexts);
}
