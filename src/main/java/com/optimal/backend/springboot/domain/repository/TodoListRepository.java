package com.optimal.backend.springboot.domain.repository;

import com.optimal.backend.springboot.domain.entity.ToDo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ToDoRepository extends JpaRepository<ToDo, UUID> {
    List<ToDo> findByUserId(UUID userId);
}
