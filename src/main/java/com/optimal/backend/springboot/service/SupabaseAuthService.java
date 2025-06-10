package com.optimal.backend.springboot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Service for handling Supabase authentication
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SupabaseAuthService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.anonKey}")
    private String supabaseAnonKey;

    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;

    /**
     * Register a new user with Supabase
     */
    public Mono<JsonNode> registerUser(String email, String password) {
        log.info("Attempting to register user with email: {}", email);
        log.debug("Using Supabase URL: {}", supabaseUrl);
        log.debug("Using anon key length: {}", supabaseAnonKey.length());
        
        WebClient webClient = webClientBuilder
                .baseUrl(supabaseUrl + "/auth/v1")
                .build();

        Map<String, Object> requestBody = Map.of(
                "email", email,
                "password", password
        );

        return webClient.post()
                .uri("/signup")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header("apikey", supabaseAnonKey)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + supabaseAnonKey)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> {
                    try {
                        log.debug("Registration response: {}", response);
                        return objectMapper.readTree(response);
                    } catch (Exception e) {
                        log.error("Error parsing registration response", e);
                        throw new RuntimeException("Failed to parse registration response", e);
                    }
                })
                .doOnSuccess(response -> log.info("User registration successful for email: {}", email))
                .doOnError(error -> {
                    if (error instanceof WebClientResponseException) {
                        WebClientResponseException webClientException = (WebClientResponseException) error;
                        log.error("Registration failed for email: {} with status: {} and body: {}", 
                                email, webClientException.getStatusCode(), webClientException.getResponseBodyAsString());
                        log.error("Request headers were: apikey={}, Authorization=Bearer {}", 
                                supabaseAnonKey.substring(0, 20) + "...", supabaseAnonKey.substring(0, 20) + "...");
                    } else {
                        log.error("Registration failed for email: {}", email, error);
                    }
                });
    }

    /**
     * Authenticate user with Supabase
     */
    public Mono<JsonNode> loginUser(String email, String password) {
        WebClient webClient = webClientBuilder
                .baseUrl(supabaseUrl + "/auth/v1")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .defaultHeader("apikey", supabaseAnonKey)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + supabaseAnonKey)
                .build();

        String formData = "grant_type=password&email=" + email + "&password=" + password;

        return webClient.post()
                .uri("/token")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .bodyValue(formData)
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> {
                    try {
                        return objectMapper.readTree(response);
                    } catch (Exception e) {
                        log.error("Error parsing login response", e);
                        throw new RuntimeException("Failed to parse login response", e);
                    }
                })
                .doOnSuccess(response -> log.info("User login successful"))
                .doOnError(error -> {
                    if (error instanceof WebClientResponseException) {
                        WebClientResponseException webClientException = (WebClientResponseException) error;
                        log.error("Login failed with status: {} and body: {}", 
                                webClientException.getStatusCode(), webClientException.getResponseBodyAsString());
                    } else {
                        log.error("Login failed", error);
                    }
                });
    }
} 