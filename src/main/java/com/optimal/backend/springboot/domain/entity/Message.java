// src/main/java/com/optimal/backend/springboot/domain/entity/Message.java
package com.optimal.backend.springboot.domain.entity;

import jakarta.persistence.*;
import java.util.UUID;
import java.sql.Timestamp;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

@Entity
@Table(name = "messages")
public class Message {

    @Id
    @Column(name = "message_id")
    private UUID messageId;

    @Column(name = "conversation_id", nullable = false)
    private UUID conversationId;

    @Column(name = "role", nullable = false)
    private String role; // "user" or "assistant"

    @Lob
    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "sequence_index", nullable = false)
    private Integer sequenceIndex;

    @Column(name = "timestamp", nullable = false)
    private Timestamp timestamp;

    @PrePersist
    protected void onCreate() {
        if (this.timestamp == null) {
            this.timestamp = new Timestamp(System.currentTimeMillis());
        }
    }
}