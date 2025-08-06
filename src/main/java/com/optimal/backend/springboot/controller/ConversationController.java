// src/main/java/com/optimal/backend/springboot/controller/ConversationController.java
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

import com.optimal.backend.springboot.database.entity.Conversation;
import com.optimal.backend.springboot.database.repository.ConversationRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/conversations")
public class ConversationController {

    private final ConversationRepository conversationRepository;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Conversation>> getConversationsByUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(conversationRepository.findByUserId(userId));
    }

    @PostMapping
    public ResponseEntity<Conversation> createConversation(@RequestBody Conversation convo) {
        convo.setConversationId(UUID.randomUUID());
        return ResponseEntity.ok(conversationRepository.save(convo));
    }
}