package com.optimal.backend.springboot.database.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Created on Ağustos, 2020
 *
 * @author Faruk
 */
@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User {

	@Id
	private UUID id;

	private String name;

	@Column(unique = true)
	private String username;

	private String password;

	@Column(unique = true)
	private String email;

	@Enumerated(EnumType.STRING)
	private UserRole userRole;
	@Override
	public String toString() {
		return "User{id=" + getId() + 
			   ", name='" + getName() + '\'' +
			   ", username='" + getUsername() + '\'' +
			   ", email='" + getEmail() + '\'' +
			   ", role='" + (getUserRole() != null ? getUserRole().toString() : "null") + "'}";
	}
	
}
