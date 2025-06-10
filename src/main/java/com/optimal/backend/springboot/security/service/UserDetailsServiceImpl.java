package com.optimal.backend.springboot.security.service;

import com.optimal.backend.springboot.model.User;
import com.optimal.backend.springboot.model.UserRole;
import com.optimal.backend.springboot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.UUID;

/**
 * UserDetailsService implementation for Supabase authentication
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

	private static final String USERNAME_OR_PASSWORD_INVALID = "Invalid username or password.";

	private final UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String supabaseUserId) throws UsernameNotFoundException {
		try {
			// Parse the Supabase user ID from JWT subject
			UUID userId = UUID.fromString(supabaseUserId);
			
			// Find user by Supabase ID
			User user = userRepository.findById(userId).orElse(null);
			
			if (user == null) {
				log.warn("User not found with Supabase ID: {}", supabaseUserId);
				throw new UsernameNotFoundException(USERNAME_OR_PASSWORD_INVALID);
			}

			// Return UserDetails with user information
			UserRole userRole = user.getUserRole() != null ? user.getUserRole() : UserRole.USER;
			SimpleGrantedAuthority grantedAuthority = new SimpleGrantedAuthority(userRole.name());

			return org.springframework.security.core.userdetails.User.builder()
					.username(user.getUsername())
					.password("") // No password needed for JWT
					.authorities(Collections.singletonList(grantedAuthority))
					.build();
					
		} catch (IllegalArgumentException e) {
			log.error("Invalid UUID format for Supabase user ID: {}", supabaseUserId);
			throw new UsernameNotFoundException(USERNAME_OR_PASSWORD_INVALID);
		}
	}
}
