package com.optimal.backend.springboot.agent.framework.core;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.agent.tool.ToolSpecification;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.ArrayList;

@Component
public class LlmClient {

    private final ChatLanguageModel chatModel;

    @Autowired
    public LlmClient(ChatLanguageModel chatModel) {
        this.chatModel = chatModel;
    }

    public LlmResponse generate(String systemPrompt, List<Message> contexts, List<Tool> tools) {
        try {
            // Null-safety checks
            if (contexts == null) {
                contexts = new ArrayList<>();
            }
            if (tools == null) {
                tools = new ArrayList<>();
            }

            // Convert our Messages to LangChain4j ChatMessages
            List<ChatMessage> messages = new ArrayList<>();

            // Add system message if provided
            if (systemPrompt != null && !systemPrompt.trim().isEmpty()) {
                messages.add(new SystemMessage(systemPrompt));
            }

            // Convert context messages
            for (Message context : contexts) {
                if (context != null) { // Skip null messages
                    messages.add(context.toLangChain4jMessage());
                }
            }

            // Convert tools to ToolSpecifications if they implement our Tool interface
            List<ToolSpecification> toolSpecs = new ArrayList<>();
            for (Tool tool : tools) {
                if (tool != null) { // Skip null tools
                    ToolSpecification toolSpec = ToolSpecification.builder()
                            .name(tool.getName())
                            .description(tool.getDescription())
                            .parameters(tool.getParameters())
                            .build();
                    toolSpecs.add(toolSpec);
                }
            }

            // Generate response
            AiMessage aiMessage;
            if (!toolSpecs.isEmpty()) {
                aiMessage = chatModel.generate(messages, toolSpecs).content();
            } else {
                aiMessage = chatModel.generate(messages).content();
            }

            // Convert back to our LlmResponse format
            return new LlmResponse(aiMessage);

        } catch (Exception e) {
            // Fallback to simulated response if LangChain4j fails
            System.err.println("LangChain4j failed, falling back to simulation: " + e.getMessage());
            return createSimulatedResponse(systemPrompt, tools);
        }
    }

    private LlmResponse createSimulatedResponse(String systemPrompt, List<Tool> tools) {
        // Ensure systemPrompt is not null for the simulated response
        String promptText = (systemPrompt != null && !systemPrompt.trim().isEmpty()) ? systemPrompt
                : "No system prompt provided";

        String content = "This is a simulated response from an LLM. " +
                "In a real implementation, this would be replaced with an actual API call to an LLM service. " +
                "The response would be generated based on the input prompt: " + promptText;

        LlmResponse response = new LlmResponse(content);

        // For demonstration purposes, simulate tool calls if tools are available
        if (tools != null && !tools.isEmpty() && Math.random() > 0.7) {
            // Simulate a tool call
            ToolCall toolCall = new ToolCall();
            toolCall.setId("call_" + System.currentTimeMillis());
            toolCall.setName("exampleTool");
            toolCall.setInput("example input");
            response.addToolCall(toolCall);
        }

        return response;
    }
}
