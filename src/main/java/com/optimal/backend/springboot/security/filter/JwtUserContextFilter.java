package com.optimal.backend.springboot.security.filter;

import com.optimal.backend.springboot.agent.framework.core.UserContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Filter to populate ThreadLocal UserContext from JWT.
 * This ensures that services and agents have access to the current user ID.
 */
public class JwtUserContextFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication != null && authentication.getPrincipal() instanceof Jwt) {
                Jwt jwt = (Jwt) authentication.getPrincipal();
                String sub = jwt.getSubject();
                
                if (sub != null) {
                    try {
                        UserContext.setUserId(UUID.fromString(sub));
                    } catch (IllegalArgumentException e) {
                        logger.warn("Invalid UUID in JWT subject: " + sub);
                    }
                }
            }
            
            filterChain.doFilter(request, response);
            
        } finally {
            // Clear context after request to prevent memory leaks in thread pool
            UserContext.clear();
        }
    }
}

