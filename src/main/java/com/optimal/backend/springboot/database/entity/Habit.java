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
@Table(name = "habits")
public class Habit {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private UUID id;

	@Column(name = "type")
	private String type;

	@Column(name = "cadence_rule")
	private String cadenceRule;

	@Column(name = "adherence_policy")
	private String adherencePolicy;

	@Column(name = "verification_method")
	private String verificationMethod;

	@Column(name = "health_score", columnDefinition = "integer default 50") // Halfway on the health bar. Under 50 is in the red zone. Above 50 is in the green zone. 
	private Integer healthScore;

	@Column(name = "notify_mode")
	private String notifyMode;

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


