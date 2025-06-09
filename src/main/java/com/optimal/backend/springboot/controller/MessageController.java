// src/main/java/com/optimal/backend/springboot/controller/MessageController.java
package com.optimal.backend.springboot.controller;

import com.optimal.backend.springboot.domain.entity.Message;
import com.optimal.backend.springboot.domain.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.sql.Timestamp;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageRepository messageRepository;

    @GetMapping("/conversation/{conversationId}")
    public ResponseEntity<List<Message>> getMessagesByConversation(@PathVariable UUID conversationId) {
        return ResponseEntity.ok(messageRepository.findByConversationIdOrderBySequenceIndex(conversationId));
    }

    @PostMapping
    public ResponseEntity<Message> createMessage(@RequestBody Message msg) {
        msg.setMessageId(UUID.randomUUID());
        msg.setTimestamp(new Timestamp(System.currentTimeMillis()));
        return ResponseEntity.ok(messageRepository.save(msg));
    }
}