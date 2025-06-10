package com.optimal.backend.springboot.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

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

    @GetMapping("/supabase-config")
    public ResponseEntity<Map<String, Object>> testSupabaseConfig() {
        return ResponseEntity.ok(Map.of(
                "supabaseUrl", supabaseUrl,
                "anonKeyLength", supabaseAnonKey.length(),
                "anonKeyPrefix", supabaseAnonKey.substring(0, Math.min(20, supabaseAnonKey.length())) + "...",
                "status", "Configuration loaded successfully"
        ));
    }
} 