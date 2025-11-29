package com.optimal.backend.springboot.configuration;

import com.optimal.backend.springboot.security.filter.JwtUserContextFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.crypto.spec.SecretKeySpec;
import java.util.Arrays;
import java.util.List;

/**
 * Security configuration for Supabase authentication
 */
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

	@Value("${supabase.jwtSecret}")
	private String jwtSecret;

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		return http
				.csrf(csrf -> csrf.disable())
				.cors(cors -> cors.configurationSource(corsConfigurationSource()))
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth
						// Public endpoints - completely bypass security
						.requestMatchers("/api/auth/register", "/api/auth/login", "/api/auth/refresh").permitAll()
						.requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
						// Protected endpoints - require JWT authentication
						.requestMatchers("/api/auth/checkAuth").authenticated()
						.requestMatchers("/actuator/**").authenticated()
						.requestMatchers("/chat/**").authenticated()
						.requestMatchers("/api/**").authenticated()  
						.anyRequest().denyAll()
						)
				.oauth2ResourceServer(oauth2 -> oauth2
						.jwt(Customizer.withDefaults())
						.bearerTokenResolver(request -> {
							String requestURI = request.getRequestURI();
							if (requestURI.equals("/api/auth/register") || 
								requestURI.equals("/api/auth/login") || 
								requestURI.equals("/api/auth/refresh") ||
								requestURI.startsWith("/v3/api-docs") ||
								requestURI.startsWith("/swagger-ui")) {
								return null;
							}
							String authorization = request.getHeader("Authorization");
							if (authorization != null && authorization.startsWith("Bearer ")) {
								return authorization.substring(7);
							}
							return null;
						}))
				.addFilterAfter(new JwtUserContextFilter(), BearerTokenAuthenticationFilter.class)
				.build();
	}

	@Bean
	public JwtDecoder jwtDecoder() {
		SecretKeySpec secretKey = new SecretKeySpec(jwtSecret.getBytes(), "HmacSHA256");
		NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withSecretKey(secretKey).build();

		// Configure validators to include audience validation for Supabase
		OAuth2TokenValidator<Jwt> audienceValidator = new JwtClaimValidator<List<String>>(
				"aud", aud -> aud != null && aud.contains("authenticated"));
		OAuth2TokenValidator<Jwt> withIssuer = JwtValidators
				.createDefaultWithIssuer("https://umndbnimuswczlydijnf.supabase.co/auth/v1");
		OAuth2TokenValidator<Jwt> withAudience = new DelegatingOAuth2TokenValidator<>(withIssuer, audienceValidator);

		jwtDecoder.setJwtValidator(withAudience);
		return jwtDecoder;
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(Arrays.asList("http://localhost:8081", "http://127.0.0.1:8081"));
		configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
		configuration.setAllowedHeaders(Arrays.asList("*"));
		configuration.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}
}
