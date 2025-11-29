package com.optimal.backend.springboot.security.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

/**
 * Context object holding user information extracted from JWT.
 * This object can be injected into controllers using @CurrentUser annotation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenUserContext {

    private UUID userId;
    private String email;
    private String role;
    private Map<String, Object> claims;

}
