package com.optimal.backend.springboot.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.optimal.backend.springboot.security.dto.LoginRequest;
import com.optimal.backend.springboot.security.dto.LoginResponse;
import com.optimal.backend.springboot.security.dto.RegistrationRequest;
import com.optimal.backend.springboot.security.dto.RegistrationResponse;
import com.optimal.backend.springboot.service.SupabaseAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication controller using Supabase
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Supabase authentication operations")
public class AuthController {

    private final SupabaseAuthService supabaseAuthService;

    @PostMapping("/register")
    @Operation(summary = "Register new user", description = "Register a new user with Supabase")
    public ResponseEntity<RegistrationResponse> register(@Valid @RequestBody RegistrationRequest request) {
        try {
            JsonNode userNode = supabaseAuthService
                .registerUser(request.getEmail(), request.getPassword())
                .block();
    
            // dump the JSON so we can see exactly what came back
            log.info("Raw Supabase signup response: {}", userNode.toPrettyString());
    
            // Supabase signup returns the user object itself, so it has "id" at the root
            if (userNode != null && userNode.has("id")) {
                RegistrationResponse dto = new RegistrationResponse();
                dto.setMessage("User registered successfully");
                // dto.setUserId(userNode.get("id").asText());
                return ResponseEntity.status(HttpStatus.CREATED).body(dto);
            }
    
            log.warn("Signup response did not contain expected 'id' field");
            RegistrationResponse bad = new RegistrationResponse();
            bad.setMessage("Registration failed: unexpected response from Supabase");
            return ResponseEntity.badRequest().body(bad);
    
        } catch (Exception e) {
            log.error("Registration failed with exception", e);
            RegistrationResponse err = new RegistrationResponse();
            err.setMessage("Registration failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(err);
        }
    }
    


    @PostMapping("/login")
    @Operation(summary = "Login user", description = "Authenticate user with Supabase")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            JsonNode response = supabaseAuthService.loginUser(request.getEmail(), request.getPassword()).block();
            
            if (response != null && response.has("access_token")) {
                LoginResponse loginResponse = new LoginResponse();
                loginResponse.setToken(response.get("access_token").asText());
                return ResponseEntity.ok(loginResponse);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
        } catch (Exception e) {
            log.error("Login failed", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
} 