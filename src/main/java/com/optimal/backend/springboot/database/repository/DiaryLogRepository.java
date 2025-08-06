package com.optimal.backend.springboot.database.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.optimal.backend.springboot.database.entity.DiaryLog;

@Repository
public interface DiaryLogRepository extends JpaRepository<DiaryLog, UUID> {
    List<DiaryLog> findAllByUserId(UUID userId);

    List<DiaryLog> findByUserIdOrderByDateDesc(UUID userId);
}
