package com.optimal.backend.springboot.database.entity;

import java.sql.Timestamp;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "habit_actions")
public class HabitAction {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private UUID id;

	@Column(name = "habit_id", nullable = false)
	private UUID habitId;

	@Column(name = "title", nullable = false)
	private String title;

	@Column(name = "recurrence_rule")
	private String recurrenceRule;

	@Column(name = "last_completed_at")
	private Timestamp lastCompletedAt;

	@Column(name = "created_at")
	private Timestamp createdAt;

	@Column(name = "updated_at")
	private Timestamp updatedAt;

	@PrePersist
	protected void onCreate() {
		if (this.createdAt == null) {
			this.createdAt = new Timestamp(System.currentTimeMillis());
		}
		if (this.updatedAt == null) {
			this.updatedAt = new Timestamp(System.currentTimeMillis());
		}
	}

	@PreUpdate
	protected void onUpdate() {
		this.updatedAt = new Timestamp(System.currentTimeMillis());
	}
}


