// src/main/java/com/optimal/backend/springboot/domain/entity/Conversation.java
package com.optimal.backend.springboot.database.entity;

import java.sql.Timestamp;
import java.util.UUID;

import com.optimal.backend.springboot.utils.DateUtils;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

@Entity
@Table(name = "conversations")
public class Conversation {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "title")
    private String title;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;

    @Column(name = "is_archived")
    private Boolean isArchived;

    @PrePersist
    protected void onCreate() {
        Timestamp now = DateUtils.getCurrentTimestamp();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = DateUtils.getCurrentTimestamp();
    }
}