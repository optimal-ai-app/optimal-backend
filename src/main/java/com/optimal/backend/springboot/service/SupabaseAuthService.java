package com.optimal.backend.springboot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
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
@Service
public class SupabaseAuthService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.anonKey}")
    private String supabaseAnonKey;

    @Autowired
    private WebClient.Builder webClientBuilder;
    
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Register a new user with Supabase
     */
    public Mono<JsonNode> registerUser(String email, String password) {
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
                        return objectMapper.readTree(response);
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to parse registration response", e);
                    }
                })
                .doOnError(error -> {
                    if (error instanceof WebClientResponseException) {
                        WebClientResponseException webClientException = (WebClientResponseException) error;
                        System.err.println("Registration failed with status: " + webClientException.getStatusCode() + 
                                " and body: " + webClientException.getResponseBodyAsString());
                    }
                });
    }

    /**
     * Authenticate user with Supabase - Fixed to send JSON instead of form data
     */
    public Mono<JsonNode> loginUser(String email, String password) {
        WebClient webClient = webClientBuilder
                .baseUrl(supabaseUrl + "/auth/v1")
                .build();

        Map<String, Object> requestBody = Map.of(
                "email", email,
                "password", password
        );

        return webClient.post()
                .uri("/token?grant_type=password")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header("apikey", supabaseAnonKey)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + supabaseAnonKey)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> {
                    try {
                        return objectMapper.readTree(response);
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to parse login response", e);
                    }
                })
                .doOnError(error -> {
                    if (error instanceof WebClientResponseException) {
                        WebClientResponseException webClientException = (WebClientResponseException) error;
                        System.err.println("Login failed with status: " + webClientException.getStatusCode() + 
                                " and body: " + webClientException.getResponseBodyAsString());
                    }
                });
    }
} 