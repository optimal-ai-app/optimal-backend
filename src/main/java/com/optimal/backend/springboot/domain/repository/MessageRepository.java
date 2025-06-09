// src/main/java/com/optimal/backend/springboot/domain/repository/MessageRepository.java
package com.optimal.backend.springboot.domain.repository;

import com.optimal.backend.springboot.domain.entity.Message;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {

    @Query("SELECT COALESCE(MAX(m.sequenceIndex), 0) FROM Message m WHERE m.conversationId = :cid")
    int findMaxSequenceIndex(@Param("cid") UUID conversationId);

    List<Message> findByConversationIdOrderBySequenceIndex(UUID conversationId);
}