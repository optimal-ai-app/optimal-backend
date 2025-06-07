package com.optimal.backend.springboot.agent.framework.agents.prompts.core;

public abstract class BasePrompt {

    protected String prompt;

    public BasePrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getPrompt() {
        return this.prompt;
    }

    protected void setPrompt(String prompt) {
        this.prompt = prompt;
    }
}
