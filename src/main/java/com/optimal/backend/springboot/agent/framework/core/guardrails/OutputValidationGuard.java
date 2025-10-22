package com.optimal.backend.springboot.agent.framework.core.guardrails;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.guardrail.OutputGuardrail;
import dev.langchain4j.guardrail.OutputGuardrailResult;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.optimal.backend.springboot.agent.framework.core.LlmClient;
import com.optimal.backend.springboot.agent.framework.core.LlmResponse;
import com.optimal.backend.springboot.agent.framework.core.Message;

@Component
public class OutputValidationGuard implements OutputGuardrail {

    final String FORMAT_FIXER_PROMPT = """
            You are a formatter. Your job is take any input in, extract text from it and output it in the following JSON format:
            {
                "content": “<input>”,
                "readyToHandoff": false,
                "reInterpret": false
            }
            
            """;

    @Autowired
    private LlmClient llmClient;

    @Override
    public OutputGuardrailResult validate(AiMessage assistantMessage) {

        String text = assistantMessage.text().replaceAll("```json", "").replace("```", "");
        text = text.replaceAll("\n", "").replaceAll("\"summary\":", "\"content\":");
        
        try {
            int objStart = text.indexOf("{");
            text = text.substring(objStart);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(text);
            if (jsonNode.has("content") && (jsonNode.has("readyToHandoff") || jsonNode.has("reInterpret"))) {
                return successWith(text);
            } else {
                throw new Exception("Invalid JSON");
            }
        } catch (Exception e) {
            List<Message> contexts = new ArrayList<Message>(List.<Message>of(new Message("user", text)));
            LlmResponse response = llmClient.generate(FORMAT_FIXER_PROMPT, contexts);
            return successWith(response.getContent());
        }
    }
}
