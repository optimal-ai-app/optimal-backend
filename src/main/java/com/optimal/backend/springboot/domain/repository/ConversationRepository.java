// src/main/java/com/optimal/backend/springboot/domain/repository/ConversationRepository.java
package com.optimal.backend.springboot.domain.repository;

import com.optimal.backend.springboot.domain.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, UUID> {
    List<Conversation> findByUserId(UUID userId);
}