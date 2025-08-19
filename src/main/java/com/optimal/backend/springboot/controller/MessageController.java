// src/main/java/com/optimal/backend/springboot/controller/MessageController.java
package com.optimal.backend.springboot.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.optimal.backend.springboot.database.entity.Message;
import com.optimal.backend.springboot.database.repository.MessageRepository;

import lombok.RequiredArgsConstructor;

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
        msg.setId(UUID.randomUUID());
        return ResponseEntity.ok(messageRepository.save(msg));
    }
}