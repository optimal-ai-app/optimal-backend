package com.optimal.backend.springboot.controller;

import com.optimal.backend.springboot.model.User;
import com.optimal.backend.springboot.model.UserRole;
import com.optimal.backend.springboot.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * Controller to sync user profiles from Supabase JWT to local database
 */
@RestController
@RequestMapping("/api/user")
public class UserSyncController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/sync-profile")
    public ResponseEntity<Map<String, Object>> syncUserProfile(Authentication authentication) {
        try {
            if (!(authentication.getPrincipal() instanceof Jwt)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid authentication"));
            }

            Jwt jwt = (Jwt) authentication.getPrincipal();
            String userId = jwt.getSubject();
            String email = jwt.getClaimAsString("email");
            
            UUID userUuid = UUID.fromString(userId);
            
            // Check if user already exists
            if (userRepository.findById(userUuid).isPresent()) {
                return ResponseEntity.ok(Map.of(
                    "message", "User profile already exists",
                    "userId", userId,
                    "email", email
                ));
            }

            // Create new user profile
            User newUser = new User();
            newUser.setId(userUuid);
            newUser.setEmail(email);
            
            // Generate username from email
            String username = email.contains("@") ? email.substring(0, email.indexOf("@")) : "user_" + System.currentTimeMillis();
            newUser.setUsername(username);
            newUser.setUserRole(UserRole.USER);

            User savedUser = userRepository.save(newUser);

            return ResponseEntity.ok(Map.of(
                "message", "User profile created successfully",
                "userId", savedUser.getId().toString(),
                "email", savedUser.getEmail(),
                "username", savedUser.getUsername()
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to sync user profile: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/current")
    public ResponseEntity<Map<String, Object>> getCurrentUser(Authentication authentication) {
        try {
            if (!(authentication.getPrincipal() instanceof Jwt)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid authentication"));
            }

            Jwt jwt = (Jwt) authentication.getPrincipal();
            String userId = jwt.getSubject();
            String email = jwt.getClaimAsString("email");
            
            UUID userUuid = UUID.fromString(userId);
            
            // Check if user exists in local database
            boolean existsInLocal = userRepository.findById(userUuid).isPresent();
            
            return ResponseEntity.ok(Map.of(
                "userId", userId,
                "email", email,
                "existsInLocalDb", existsInLocal,
                "jwtClaims", Map.of(
                    "iss", jwt.getIssuer(),
                    "aud", jwt.getAudience(),
                    "exp", jwt.getExpiresAt(),
                    "iat", jwt.getIssuedAt()
                )
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to get current user: " + e.getMessage()
            ));
        }
    }
} 