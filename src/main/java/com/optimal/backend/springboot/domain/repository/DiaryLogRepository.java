package com.optimal.backend.springboot.domain.repository;

import com.optimal.backend.springboot.domain.entity.DiaryLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DiaryLogRepository extends JpaRepository<DiaryLog, UUID> {
    List<DiaryLog> findAllByUserId(UUID userId);

    List<DiaryLog> findByUserIdOrderByDateDesc(UUID userId);
}
