package com.optimal.backend.springboot.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.optimal.backend.springboot.database.entity.Conversation;
import com.optimal.backend.springboot.database.entity.Message;
import com.optimal.backend.springboot.database.repository.ConversationRepository;
import com.optimal.backend.springboot.database.repository.MessageRepository;
import com.optimal.backend.springboot.utils.DateUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final SummarizationClient summarizationClient;

    /**
     * Creates a new conversation for the user. Pruning is handled by the DB trigger.
     */
    @Transactional
    public Conversation createConversation(UUID userId, String title) {
        Conversation convo = new Conversation();
        convo.setConversationId(UUID.randomUUID());
        convo.setUserId(userId);
        convo.setTitle(title);
        convo.setSummaryText("");
        return conversationRepository.save(convo);
    }

    /**
     * Adds a user message to the conversation.
     */
    @Transactional
    public Message addUserMessage(UUID conversationId, String content) {
        int nextIdx = messageRepository.findMaxSequenceIndex(conversationId) + 1;
        Message msg = new Message();
        msg.setMessageId(UUID.randomUUID());
        msg.setConversationId(conversationId);
        msg.setRole("user");
        msg.setContent(content);
        msg.setSequenceIndex(nextIdx);
        return messageRepository.save(msg);
    }

    /**
     * Adds the assistant's reply and updates the rolling summary.
     */
    @Transactional
    public Message addAssistantMessage(UUID conversationId, String content) {
        int nextIdx = messageRepository.findMaxSequenceIndex(conversationId) + 1;
        Message msg = new Message();
        msg.setMessageId(UUID.randomUUID());
        msg.setConversationId(conversationId);
        msg.setRole("assistant");
        msg.setContent(content);
        msg.setSequenceIndex(nextIdx);
        msg.setCreatedAt(DateUtils.getCurrentTimestamp());
        Message assistantMsg = messageRepository.save(msg);

        // Fetch full history for summarization
        List<Message> history = messageRepository.findByConversationIdOrderBySequenceIndex(conversationId);
        List<String> texts = new ArrayList<>();
        for (Message m : history) {
            texts.add(m.getRole() + ": " + m.getContent());
        }

        Conversation convo = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalStateException("Conversation not found"));
        String updated = summarizationClient.updateSummary(convo.getSummaryText(), texts);
        convo.setSummaryText(updated);
        conversationRepository.save(convo);

        return assistantMsg;
    }
}