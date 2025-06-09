package com.optimal.backend.springboot.service;

import com.optimal.backend.springboot.domain.entity.Conversation;
import com.optimal.backend.springboot.domain.entity.Message;
import com.optimal.backend.springboot.domain.repository.ConversationRepository;
import com.optimal.backend.springboot.domain.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.sql.Timestamp;

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
        msg.setTimestamp(new Timestamp(System.currentTimeMillis()));
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
        msg.setTimestamp(new Timestamp(System.currentTimeMillis()));
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