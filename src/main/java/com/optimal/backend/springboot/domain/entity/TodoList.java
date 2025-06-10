package com.optimal.backend.springboot.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.UUID;
import java.util.List;
import java.util.ArrayList;
import java.sql.Timestamp;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "todo_lists")
public class TodoList {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "todo_list_id")
    private UUID todoListId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @OneToMany(mappedBy = "todoList", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Task> tasks = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    private Timestamp createdAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;

    @PrePersist
    protected void onCreate() {
        if (this.todoListId == null) {
            this.todoListId = UUID.randomUUID();
        }
        this.createdAt = new Timestamp(System.currentTimeMillis());
        this.updatedAt = new Timestamp(System.currentTimeMillis());
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = new Timestamp(System.currentTimeMillis());
    }
}
