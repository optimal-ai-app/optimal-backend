package com.optimal.backend.springboot.agent.framework.core.guardrails;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.guardrail.InputGuardrail;
import dev.langchain4j.guardrail.InputGuardrailResult;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class PromptInjectionGuard implements InputGuardrail {

    private static final List<String> SUSPICIOUS_PATTERNS = List.of(
            "ignore previous instructions",
            "ignore all previous",
            "disregard",
            "forget everything",
            "new instructions",
            "you are now",
            "system:",
            "assistant:",
            "ADMIN MODE");

    @Override
    public InputGuardrailResult validate(UserMessage userMessage) {
        String text = userMessage.singleText().toLowerCase();
        for (String pattern : SUSPICIOUS_PATTERNS) {
            if (text.contains(pattern.toLowerCase())) {
                return failure(
                        "Prompt contains disallowed pattern: \"" + pattern + "\"; please rephrase.");
            }
        }
        return InputGuardrailResult.success();
    }
}
