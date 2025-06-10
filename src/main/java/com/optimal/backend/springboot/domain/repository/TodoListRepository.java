package com.optimal.backend.springboot.domain.repository;

import com.optimal.backend.springboot.domain.entity.TodoList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TodoListRepository extends JpaRepository<TodoList, UUID> {
    List<TodoList> findByUserId(UUID userId);
}
