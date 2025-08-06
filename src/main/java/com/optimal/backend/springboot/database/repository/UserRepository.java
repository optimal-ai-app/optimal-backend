package com.optimal.backend.springboot.database.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.optimal.backend.springboot.database.entity.User;

/**
 * Created on Ağustos, 2020
 *
 * @author Faruk
 */
public interface UserRepository extends JpaRepository<User, UUID> {

	User findByUsername(String username);

	boolean existsByEmail(String email);

	boolean existsByUsername(String username);

}
