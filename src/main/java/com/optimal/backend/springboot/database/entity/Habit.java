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
@Table(name = "habits")
public class Habit {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private UUID id;

	@Column(name = "title")
	private String title;

	@Column(name = "description")
	private String description;

	@Column(name = "streak")
	private Integer streak;

	@Column(name = "cadence")
	private String cadence;

	@Column(name = "health")
	private Integer health;

	@Column(name = "is_active")
	private Boolean isActive;

	@Column(name = "created_at")
	private Timestamp createdAt;

	@Column(name = "tags")
	private String tags;

	@Column(name = "user_id")
	private UUID userId;

	@PrePersist
	protected void onCreate() {
		if (this.createdAt == null) {
			this.createdAt = new Timestamp(System.currentTimeMillis());
		}
	}
}


