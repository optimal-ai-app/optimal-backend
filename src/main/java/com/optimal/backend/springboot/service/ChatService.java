package com.optimal.backend.springboot.service;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.optimal.backend.springboot.database.entity.Conversation;
import com.optimal.backend.springboot.database.entity.Message;
import com.optimal.backend.springboot.database.repository.ConversationRepository;
import com.optimal.backend.springboot.database.repository.MessageRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class ChatService {

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private  MessageRepository messageRepository;

    /**
     * Creates a new conversation for the user. Pruning is handled by the DB
     * trigger.
     */
    public Conversation createConversation(UUID userId, String title) {
        Conversation convo = new Conversation();
        convo.setId(UUID.randomUUID());
        convo.setUserId(userId);
        convo.setTitle(title);
        return conversationRepository.save(convo);
    }

    /**
     * Adds a user message to the conversation with retry logic for optimistic locking.
     */
    // @Retryable(
    //     value = ObjectOptimisticLockingFailureException.class,
    //     maxAttempts = 3,
    //     backoff = @Backoff(delay = 100, multiplier = 2)
    // )
    public void addUserMessage(UUID conversationId, UUID userId, String content) {
        try {
            //check if the conversation exists
            System.out.println("conversationId: " + conversationId);
            Optional<Conversation> convo = conversationRepository.findById(conversationId);
            if(!convo.isPresent()) {
                System.out.println("conversation does not exist, creating new one");
                Conversation newConvo = new Conversation();
                newConvo.setId(conversationId);
                newConvo.setUserId(userId);
                newConvo.setTitle(content);
                convo = Optional.of(conversationRepository.save(newConvo));
            }
            System.out.println("addming message");
            Message msg = new Message();
            msg.setConversationId(convo.get().getId());
            msg.setRole("user");
            msg.setContent(content);
            messageRepository.save(msg);
        } catch (ObjectOptimisticLockingFailureException e) {
            log.warn("Optimistic locking failure for conversation {}: {}", conversationId, e.getMessage());
            throw e; // Re-throw to trigger retry
        }
    }

    /**
     * Adds the assistant's reply and updates the rolling summary with retry logic.
     */
    @Transactional
    @Retryable(
        value = ObjectOptimisticLockingFailureException.class,
        maxAttempts = 3,
        backoff = @Backoff(delay = 100, multiplier = 2)
    )
    public Message addAgentMessage(UUID conversationId, UUID userId, String content) {
        try {
            Conversation convo = conversationRepository.findById(conversationId)
                    .orElseGet(() -> {
                        Conversation newConvo = new Conversation();
                        newConvo.setId(conversationId);
                        newConvo.setUserId(userId);
                        newConvo.setTitle(content);
                        return conversationRepository.save(newConvo);
                    });
            Message msg = new Message();
            msg.setConversationId(convo.getId());
            msg.setRole("agent");
            msg.setContent(content);
            return messageRepository.save(msg);
        } catch (ObjectOptimisticLockingFailureException e) {
            log.warn("Optimistic locking failure for conversation {}: {}", conversationId, e.getMessage());
            throw e; // Re-throw to trigger retry
        }
    }
}