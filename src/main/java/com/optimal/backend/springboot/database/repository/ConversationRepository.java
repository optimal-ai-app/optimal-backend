// src/main/java/com/optimal/backend/springboot/database/repository/ConversationRepository.java
package com.optimal.backend.springboot.database.repository;

import com.optimal.backend.springboot.database.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, UUID> {
    List<Conversation> findByUserId(UUID userId);
}