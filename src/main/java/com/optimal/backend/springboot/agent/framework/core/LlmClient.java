package com.optimal.backend.springboot.agent.framework.core;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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
            You are a meta-coordinator that analyzes conversation flow and identifies which step number from an agent's system prompt should be executed next.

            Analysis Process
            1. Identify the agent type from the system prompt
            2. Parse the system prompt structure to find discrete instruction sections/steps (numbered or labeled)
            3. Analyze recent messages to determine conversation state:
               - What has the user provided?
               - What was the agent's last action/question?
               - Is the user confirming/completing something?
               - Is the user providing new information?
               - Is the user stuck or confused?
            4. Determine which step number is most relevant for the NEXT response

            State Detection Patterns
            - Starting conversation: Return first step number
            - Mid-flow: User answering questions → Return next logical step number
            - User provided multiple things at once: Return step number that handles bulk input
            - Confirmation detected ("yes", "confirm", "add", "submit", "looks good"):
              Return step number for completion/finalization
            - User seems confused/stuck: Return step number for clarification or previous step
            - User requests change: Return step number for modification/edit

            Output Format
            Return ONLY the step number as a single integer.
            No text, no explanation, no punctuation - just the number.

            Examples:
            - If Step 3 should be executed next, output: 3
            - If instruction section labeled "Step 7" is relevant, output: 7

            Input
            Recent Messages: [INSERT_LAST_3_MESSAGES]
            Agent System Prompt: [INSERT_FULL_SYSTEM_PROMPT]
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
            ChatModel lightModel = applicationContext.getBean(LangChain4jConfig.class)
                    .lightChatLanguageModel();
            ChatResponse response = lightModel.chat(allMessages);
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
            ChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(1000);
            UUID uuid = UUID.randomUUID();
            chatMemory.add(SystemMessage.from(uuid + systemPrompt));

            for (Message m : messages) {
                chatMemory.add(m.toLangChain4jMessage());
            }
            if (systemPrompt != null && !systemPrompt.isEmpty()) {
                if (currentStep > 0) {
                    StringBuilder addedPrompt = new StringBuilder();
                    String[] systemPromptSections = systemPrompt.split("<SECTION>");
                    addedPrompt.append(systemPromptSections[0]);
                    String[] promptSteps = systemPromptSections[1].split("Step ");
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
                        String contextInput;
                        if (count < 4) {
                            contextInput = String.format(
                                    "Last 2 messages:\n%s\n%s\n\nSystem prompt:\n%s",
                                    messages.get(count - 2).getTextContent(),
                                    messages.get(count - 1).getTextContent(),
                                    systemPromptSections[1]);
                        } else {
                            contextInput = String.format(
                                    "Last 4 messages:\n%s\n%s\n%s\n%s\n\nSystem prompt:\n%s",
                                    messages.get(count - 4).getTextContent(),
                                    messages.get(count - 3).getTextContent(),
                                    messages.get(count - 2).getTextContent(),
                                    messages.get(count - 1).getTextContent(),
                                    systemPromptSections[1]);
                        }

                        ChatModel instructionModel = applicationContext.getBean(LangChain4jConfig.class)
                                .lightChatLanguageModel();
                        ToolCapableAssistant instructionAgent = AiServices.builder(ToolCapableAssistant.class)
                                .chatModel(instructionModel)
                                .chatMemory(selectorMemory)
                                .build();

                        Response<AiMessage> instruction = instructionAgent.chat(contextInput);
                        int instructionStep = Integer.parseInt(instruction.content().text().trim());
                        addedPrompt
                                .append("\n\n[INSTRUCTION: " + promptSteps[instructionStep] +
                                        "]\n\n");
                        chatMemory.add(SystemMessage.from(addedPrompt.toString()));
                        System.out.println("PROMPT: " + addedPrompt.toString());
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

            return new LlmResponse(response.content().text().toString().replaceAll("```json", "").replace("```", ""),
                    response.tokenUsage().totalTokenCount());

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
