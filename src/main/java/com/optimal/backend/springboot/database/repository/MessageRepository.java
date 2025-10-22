// src/main/java/com/optimal/backend/springboot/domain/repository/MessageRepository.java
package com.optimal.backend.springboot.database.repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.optimal.backend.springboot.database.entity.Message;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {

  @Query(value = """
      select count(*)
      from messages m
      join conversations c on c.id = m.conversation_id
      where c.user_id = cast(:userId as uuid)
        and c.updated_at >= :systemDate and m.created_at >= :systemDate
      """, nativeQuery = true)
  long countUsersMessages(@Param("userId") String userId, @Param("systemDate") Instant systemDate);

  @Query("""
        SELECT m
          FROM Message m
         WHERE m.conversationId = :conversationId
      ORDER BY m.createdAt ASC
        """)
  List<Message> findByConversationIdOrderByCreatedAtAsc(@Param("conversationId") UUID conversationId);
}