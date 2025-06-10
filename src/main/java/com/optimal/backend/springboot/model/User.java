package com.optimal.backend.springboot.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

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
