package com.optimal.backend.springboot.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.UUID;
import java.util.Set;
import java.util.HashSet;
import java.sql.Timestamp;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "diary_logs")
public class DiaryLog {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "diary_log_id")
    private UUID diaryLogId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "entry", nullable = false, length = 2000)
    private String entry;

    @Enumerated(EnumType.STRING)
    @Column(name = "category")
    private CategoryEnum category;

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "diary_log_tags",
        joinColumns = @JoinColumn(name = "diary_log_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    @Column(name = "created_at", nullable = false)
    private Timestamp createdAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;

    @PrePersist
    protected void onCreate() {
        if (this.diaryLogId == null) {
            this.diaryLogId = UUID.randomUUID();
        }
        this.createdAt = new Timestamp(System.currentTimeMillis());
        this.updatedAt = new Timestamp(System.currentTimeMillis());
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = new Timestamp(System.currentTimeMillis());
    }
}
