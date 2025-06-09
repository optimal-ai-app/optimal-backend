// src/main/java/com/optimal/backend/springboot/controller/ConversationController.java
package com.optimal.backend.springboot.controller;

import com.optimal.backend.springboot.domain.entity.Conversation;
import com.optimal.backend.springboot.domain.repository.ConversationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

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