package com.optimal.backend.springboot.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;
import java.util.Base64;

/**
 * Test controller for Supabase configuration debugging
 */
@RestController
@RequestMapping("/api/test")
public class SupabaseTestController {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.anonKey}")
    private String supabaseAnonKey;

    @Value("${supabase.jwtSecret}")
    private String jwtSecret;

    @GetMapping("/supabase-config")
    public ResponseEntity<Map<String, Object>> testSupabaseConfig() {
        return ResponseEntity.ok(Map.of(
                "supabaseUrl", supabaseUrl,
                "anonKeyLength", supabaseAnonKey.length(),
                "anonKeyPrefix", supabaseAnonKey.substring(0, Math.min(20, supabaseAnonKey.length())) + "...",
                "jwtSecretLength", jwtSecret.length(),
                "jwtSecretPrefix", jwtSecret.substring(0, Math.min(20, jwtSecret.length())) + "...",
                "status", "Configuration loaded successfully"
        ));
    }

    @GetMapping("/decode-token-header")
    public ResponseEntity<Map<String, Object>> decodeTokenHeader(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body(Map.of("error", "No Bearer token provided"));
        }
        
        String token = authHeader.substring(7);
        
        try {
            // Decode just the header and payload for inspection (not verification)
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid JWT format"));
            }
            
            String header = new String(Base64.getUrlDecoder().decode(parts[0]));
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
            
            return ResponseEntity.ok(Map.of(
                    "tokenLength", token.length(),
                    "header", header,
                    "payload", payload,
                    "signatureLength", parts[2].length(),
                    "note", "This is for debugging only - signature not verified"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to decode token: " + e.getMessage()));
        }
    }
} 