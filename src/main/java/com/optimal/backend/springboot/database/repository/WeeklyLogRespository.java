package com.optimal.backend.springboot.database.repository;

import java.sql.Date;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.optimal.backend.springboot.database.entity.WeeklyLog;

@Repository
public interface WeeklyLogRespository extends JpaRepository<WeeklyLog, UUID> {
    @Query("SELECT w FROM WeeklyLog w WHERE w.userId = :userId AND w.endDate >= :dateSevenDaysAgo")
    Optional<WeeklyLog> findByUserIdWithinSevenDays(@Param("userId") UUID userId, @Param("dateSevenDaysAgo") Date dateSevenDaysAgo);

}
