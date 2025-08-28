package com.optimal.backend.springboot.database.repository;

import java.sql.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.optimal.backend.springboot.database.entity.Tag;

@Repository
public interface TagRepository extends JpaRepository<Tag, UUID> {
    Optional<Tag> findByNameIgnoreCase(String name);

    @Query("SELECT t FROM Tag t JOIN DiaryLog d ON t.diaryLogId = d.id WHERE d.userId = :userId AND d.date BETWEEN :startDate AND :endDate")
    List<Tag> findByUserAndDateBetween(UUID userId, Date startDate, Date endDate);
}
