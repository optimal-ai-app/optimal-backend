// src/main/java/com/optimal/backend/springboot/controller/ConversationController.java
package com.optimal.backend.springboot.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.optimal.backend.springboot.database.entity.Conversation;
import com.optimal.backend.springboot.database.repository.ConversationRepository;
import com.optimal.backend.springboot.security.annotation.CurrentUser;
import com.optimal.backend.springboot.security.model.TokenUserContext;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/conversations")
public class ConversationController {

    private final ConversationRepository conversationRepository;

    @GetMapping("/get")
    public ResponseEntity<List<Conversation>> getConversationsByUser(@CurrentUser TokenUserContext userContext) {
        return ResponseEntity.ok(conversationRepository.findByUserId(userContext.getUserId()));
    }

}