package com.optimal.backend.springboot.agent.framework.core;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.optimal.backend.springboot.service.ChatService;

@Component
public class SupervisorFactory {

    @Autowired
    private LlmClient llmClient;

    @Autowired
    private ChatService chatService;

    /**
     * Creates a new BaseSupervisor instance with all dependencies properly
     * injected.
     * This allows for stateful supervisors while maintaining Spring's dependency
     * injection.
     */
    public BaseSupervisor createSupervisor() {
        BaseSupervisor supervisor = new BaseSupervisor();
        // Manually inject the dependencies since we're creating a new instance
        supervisor.setLlmClient(llmClient);
        supervisor.setChatService(chatService);
        return supervisor;
    }
}
