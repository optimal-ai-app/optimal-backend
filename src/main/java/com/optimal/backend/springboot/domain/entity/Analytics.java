package com.optimal.backend.springboot.domain.entity;

import jakarta.persistence.*;
import java.util.UUID;
import java.sql.Timestamp;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "analytics")
@Getter
@Setter
public class Analytics {

    @Id
    private UUID id;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "metric_name")
    private String metricName;

    @Column(name = "value")
    private Double value;

    @Column(name = "recorded_at")
    private Timestamp recordedAt;
}
