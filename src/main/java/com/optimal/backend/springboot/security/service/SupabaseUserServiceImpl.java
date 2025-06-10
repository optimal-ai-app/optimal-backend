package com.optimal.backend.springboot.security.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.optimal.backend.springboot.model.User;
import com.optimal.backend.springboot.model.UserRole;
import com.optimal.backend.springboot.repository.UserRepository;
import com.optimal.backend.springboot.security.dto.AuthenticatedUserDto;
import com.optimal.backend.springboot.security.dto.RegistrationRequest;
import com.optimal.backend.springboot.security.dto.RegistrationResponse;
import com.optimal.backend.springboot.security.mapper.UserMapper;
import com.optimal.backend.springboot.service.SupabaseAuthService;
import com.optimal.backend.springboot.utils.GeneralMessageAccessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Supabase-based implementation of UserService
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SupabaseUserServiceImpl implements UserService {

    private static final String REGISTRATION_SUCCESSFUL = "registration_successful";

    private final UserRepository userRepository;
    private final SupabaseAuthService supabaseAuthService;
    private final GeneralMessageAccessor generalMessageAccessor;

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public RegistrationResponse registration(RegistrationRequest registrationRequest) {
        try {
            // Register with Supabase
            JsonNode supabaseResponse = supabaseAuthService.registerUser(
                    registrationRequest.getEmail(), 
                    registrationRequest.getPassword()
            ).block();

            if (supabaseResponse != null && supabaseResponse.has("user")) {
                JsonNode userNode = supabaseResponse.get("user");
                String supabaseUserId = userNode.get("id").asText();

                // Create local user profile (no password stored locally)
                User user = new User();
                user.setId(UUID.fromString(supabaseUserId));
                user.setName(registrationRequest.getName());
                user.setUsername(registrationRequest.getUsername());
                user.setEmail(registrationRequest.getEmail());
                user.setUserRole(UserRole.USER);

                userRepository.save(user);

                final String registrationSuccessMessage = generalMessageAccessor.getMessage(
                        null, REGISTRATION_SUCCESSFUL, registrationRequest.getUsername()
                );

                log.info("{} registered successfully with Supabase!", registrationRequest.getUsername());

                return new RegistrationResponse(registrationSuccessMessage);
            } else {
                throw new RuntimeException("Failed to register user with Supabase");
            }
        } catch (Exception e) {
            log.error("Registration failed", e);
            throw new RuntimeException("Registration failed: " + e.getMessage());
        }
    }

    @Override
    public AuthenticatedUserDto findAuthenticatedUserByUsername(String username) {
        final User user = findByUsername(username);
        return UserMapper.INSTANCE.convertToAuthenticatedUserDto(user);
    }

    /**
     * Find user by Supabase user ID
     */
    public User findBySupabaseId(UUID supabaseId) {
        return userRepository.findById(supabaseId).orElse(null);
    }
} 