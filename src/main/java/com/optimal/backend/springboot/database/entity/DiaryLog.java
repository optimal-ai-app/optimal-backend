package com.optimal.backend.springboot.database.entity;

import java.sql.Timestamp;
import java.util.UUID;

import com.optimal.backend.springboot.utils.DateUtils;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "diary_logs")
public class DiaryLog {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "diary_log_id")
    private UUID diaryLogId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "transcript", nullable = false, columnDefinition = "TEXT")
    private String transcript;

    @Column(name = "date", nullable = false)
    private Timestamp date;
    
    @Column(name = "summary", nullable = true, columnDefinition = "TEXT")
    private String summary;

    @PrePersist
    protected void onCreate() {
        if (this.diaryLogId == null) {
            this.diaryLogId = UUID.randomUUID();
        }
        this.date = DateUtils.getCurrentTimestamp();
    }
}
