package com.optimal.backend.springboot.domain.repository;

import com.optimal.backend.springboot.domain.entity.Checkin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CheckinRepository extends JpaRepository<Checkin, UUID> {
    List<Checkin> findByUserId(UUID userId);
    List<Checkin> findByToDoId(UUID todoId);
}
