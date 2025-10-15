package com.optimal.backend.springboot.agent.framework.core.guardrails;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.guardrail.OutputGuardrail;
import dev.langchain4j.guardrail.OutputGuardrailResult;

import org.springframework.stereotype.Component;

@Component
public class OutputValidationGuard implements OutputGuardrail {

    @Override
    public OutputGuardrailResult validate(AiMessage assistantMessage) {
        System.out.println("Validating Result");
        String text = assistantMessage.text().replaceAll("```json", "").replace("```", "");
        text = text.replaceAll("\n", "");

        int objStart = text.indexOf("{");
        text = text.substring(objStart);

        return successWith(text);
    }
}
