// src/main/java/com/optimal/backend/springboot/domain/repository/ConversationRepository.java
package com.optimal.backend.springboot.database.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.optimal.backend.springboot.database.entity.Conversation;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, UUID> {
    List<Conversation> findByUserId(UUID userId);
}