package com.optimal.backend.springboot.database.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.optimal.backend.springboot.database.entity.Checkin;

@Repository
public interface CheckinRepository extends JpaRepository<Checkin, UUID> {
    List<Checkin> findByUserId(UUID userId);
    List<Checkin> findByTodoId(UUID todoId);
}
