package com.optimal.backend.springboot.database.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

@Entity
@Table(name = "tags")
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(name = "diary_log_id", nullable = false)
    private UUID diaryLogId;

    public Tag(String name, UUID diaryLogId) {
        this.name = name;
        this.diaryLogId = diaryLogId;
    }
}
