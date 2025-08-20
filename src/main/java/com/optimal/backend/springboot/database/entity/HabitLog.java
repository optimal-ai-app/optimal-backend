package com.optimal.backend.springboot.database.entity;

import java.sql.Timestamp;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "habit_logs")
public class HabitLog {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private UUID id;

	@Column(name = "habit_id", nullable = false)
	private UUID habitId;

	@Column(name = "date_completed")
	private Timestamp dateCompleted;

	@Column(name = "minutes_used", nullable = false)
	private Integer minutesUsed = 0;

	@Column(name = "created_at", nullable = false)
	private Timestamp createdAt;

	@PrePersist
	protected void onCreate() {
		if (this.createdAt == null) {
			this.createdAt = new Timestamp(System.currentTimeMillis());
		}
	}
}


