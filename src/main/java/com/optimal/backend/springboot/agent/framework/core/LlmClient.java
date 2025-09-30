package com.optimal.backend.springboot.agent.framework.core;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.optimal.backend.springboot.agent.framework.config.LangChain4jConfig;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.service.AiServices;

@Component
public class LlmClient {
    @Autowired
    private ApplicationContext applicationContext;

    private ChatModel chatModel;

    @Autowired
    public LlmClient(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    public static final String INSTRUCTION_AGENT_PROMPT = """
            Task
            Read last 2 messages + system prompt. Return the ONE most relevant instruction for the next response.
            Rules

            Find current position in workflow/steps
            Return appropriate step:

            User stuck/confused → previous step
            User completed task → next step
            User needs clarification → current step
            User starting → first step

            Output
            Return instruction text exactly as written

            Input
            Messages: [INSERT_LAST_2_MESSAGES]
            System: [INSERT_SYSTEM_PROMPT]
                        """;

    public void overrideChatModel(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    /**
     * Generate response without tools using direct ChatModel
     */
    public LlmResponse generate(String systemPrompt, List<Message> messages) {
        try {
            List<ChatMessage> allMessages = new ArrayList<>();
            if (systemPrompt != null && !systemPrompt.isEmpty()) {
                allMessages.add(SystemMessage.from(systemPrompt));
            }
            allMessages.addAll(messages.stream().map(Message::toLangChain4jMessage).collect(Collectors.toList()));
            ChatResponse response = chatModel.chat(allMessages);
            AiMessage aiMessage = response.aiMessage();
            return new LlmResponse(aiMessage, response.tokenUsage().totalTokenCount());
        } catch (Exception e) {
            System.err.println("LangChain4j error: " + e.getMessage());
            return new LlmResponse("LLM ERROR");
        }
    }

    /**
     * Generate response with tools using AiService
     */
    public LlmResponse generate(String systemPrompt, List<Message> messages, List<Object> tools,
            int currentStep) {
        try {
            ChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(6);
            for (Message m : messages) {
                chatMemory.add(m.toLangChain4jMessage());
            }
            if (systemPrompt != null && !systemPrompt.isEmpty()) {
                if (currentStep < 0) {
                    chatMemory.add(SystemMessage.from(systemPrompt));
                    System.out.println("\n\nSystemPrompt");

                } else {
                    // StringBuilder addedPrompt = new StringBuilder();
                    // String[] systemPromptSections = systemPrompt.split("<SECTION>");
                    // addedPrompt.append(systemPromptSections[0]);
                    // String[] promptSteps = systemPromptSections[1].split("Step ");
                    // addedPrompt.append(promptSteps[currentStep]);
                    // addedPrompt.append("Current Step in the process: " + currentStep);
                    // chatMemory.add(SystemMessage.from(addedPrompt.toString()));
                    // System.out.println("\n\nPrompt: " + addedPrompt.toString());

                    int count = messages.size();
                    if (count < 2) {
                        chatMemory.add(SystemMessage.from(systemPrompt));
                    } else {
                        // Create clean memory for instruction selector
                        ChatMemory selectorMemory = MessageWindowChatMemory.withMaxMessages(10);
                        selectorMemory.add(SystemMessage.from(INSTRUCTION_AGENT_PROMPT));

                        // Format the input properly
                        String contextInput = String.format(
                                "Last 2 messages:\n%s\n%s\n\nSystem prompt:\n%s",
                                messages.get(count - 2).getTextContent(),
                                messages.get(count - 1).getTextContent(),
                                systemPrompt);

                        ChatModel instructionModel = applicationContext.getBean(LangChain4jConfig.class)
                                .lightChatLanguageModel();
                        ToolCapableAssistant instructionAgent = AiServices.builder(ToolCapableAssistant.class)
                                .chatModel(instructionModel)
                                .chatMemory(selectorMemory)
                                .build();

                        Response<AiMessage> instruction = instructionAgent.chat(contextInput);

                        // Handle the response properly
                        String selectedInstruction = instruction.content().text().trim();

                        System.out.println("SelectedInstruction: " + selectedInstruction);

                        String enhancedMessage = "[INSTRUCTION: " + selectedInstruction + "]\n\n";
                        chatMemory.add(SystemMessage.from(enhancedMessage));
                    }
                }
            }
            for (int i = 0; i < tools.size(); i++) {
                System.out.println("\n\nAvailable Tool: " + tools.get(i).getClass().getSimpleName());
            }
            // Create AI Service with tools
            ToolCapableAssistant assistant = AiServices.builder(ToolCapableAssistant.class)
                    .chatModel(chatModel)
                    .chatMemory(chatMemory)
                    .tools(tools)
                    .build();

            // Get the latest user message to send to assistant
            String userInput = getLatestUserMessage(messages);

            // Generate response using AI Service
            Response<AiMessage> response = assistant.chat(userInput);
            System.out.println(
                    "\n\nTokens: " + response.tokenUsage().totalTokenCount());
            System.out.println(
                    "\n\nResponse: " + response.content().text());
            // Convert response to LlmResponse
            // Note: AiServices handles tool execution internally, so we get the final
            // response
            return new LlmResponse(response.content().text(), response.tokenUsage().totalTokenCount());

        } catch (Exception e) {
            System.err.println("Error generating response with tools: " + e.getMessage());
            e.printStackTrace();
            return new LlmResponse("Error generating response with tools: " + e.getMessage());
        }
    }

    /**
     * Extract the latest user message from the message list
     */
    private String getLatestUserMessage(List<Message> messages) {
        if (messages == null || messages.isEmpty()) {
            return "Hello";
        }

        // Find the last user message
        for (int i = messages.size() - 1; i >= 0; i--) {
            Message message = messages.get(i);
            if ("user".equalsIgnoreCase(message.getRole())) {
                return message.getContent() != null ? message.getContent()
                        : message.getMessage() != null ? message.getMessage() : "Hello";
            }
        }

        // Fallback: return the last message content
        Message lastMessage = messages.get(messages.size() - 1);
        return lastMessage.getContent() != null ? lastMessage.getContent()
                : lastMessage.getMessage() != null ? lastMessage.getMessage() : "Hello";
    }

    /**
     * Assistant interface for tool-capable AI services.
     * This interface is used by AiServices to handle chat with tool support.
     */
    public interface ToolCapableAssistant {
        Response<AiMessage> chat(String userMessage);
    }

}
