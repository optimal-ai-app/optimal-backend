package com.optimal.backend.springboot.repository;

import com.optimal.backend.springboot.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
import java.util.Optional;

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
