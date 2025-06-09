package com.optimal.backend.springboot.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter

@Entity
@Table(name = "tags")
public class Tag {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    @ManyToMany(mappedBy = "tags", fetch = FetchType.LAZY)
    private Set<DiaryLog> diaryLogs = new HashSet<>();
}
