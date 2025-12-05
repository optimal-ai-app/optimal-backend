package com.optimal.backend.springboot.configuration;

import com.optimal.backend.springboot.security.filter.JwtUserContextFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Security configuration for Supabase authentication + Cloudflare Protection
 */
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

	@Value("${supabase.jwtSecret}")
	private String jwtSecret;

	// 1. Load the secret token from your properties
	@Value("${cloudflare.secretToken}")
	private String cloudflareSecretToken;

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		return http
				.csrf(csrf -> csrf.disable())
				.cors(cors -> cors.configurationSource(corsConfigurationSource()))

				// 2. Add the Cloudflare Filter HERE (Before standard auth checks)
				.addFilterBefore(new CloudflareSecretFilter(cloudflareSecretToken),
						UsernamePasswordAuthenticationFilter.class)

				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
					.authorizeHttpRequests(auth -> auth
							// Public endpoints - completely bypass security
							.requestMatchers("/api/auth/register", "/api/auth/login", "/api/auth/refresh").permitAll()
							.requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
							.requestMatchers("/evaluation/**").permitAll() // Evaluation endpoints for testing
						// Protected endpoints - require JWT authentication
						.requestMatchers("/api/auth/checkAuth").authenticated()
						.requestMatchers("/actuator/**").authenticated()
						.requestMatchers("/chat/**").authenticated()
						.requestMatchers("/api/**").authenticated()
						.anyRequest().denyAll())
				.oauth2ResourceServer(oauth2 -> oauth2
						.jwt(Customizer.withDefaults())
						// (Kept your custom resolver logic exactly as is)
						.bearerTokenResolver(request -> {
							String requestURI = request.getRequestURI();
							if (requestURI.equals("/api/auth/register") || 
								requestURI.equals("/api/auth/login") || 
								requestURI.equals("/api/auth/refresh") ||
								requestURI.startsWith("/v3/api-docs") ||
								requestURI.startsWith("/swagger-ui") ||
								requestURI.startsWith("/evaluation")) {
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

	// 3. Define the simple inner class for the Filter
	public static class CloudflareSecretFilter extends OncePerRequestFilter {
		private final String expectedSecret;

		public CloudflareSecretFilter(String expectedSecret) {
			this.expectedSecret = expectedSecret;
		}

		@Override
		protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
				FilterChain filterChain)
				throws ServletException, IOException {

			String incomingSecret = request.getHeader("X-Optimal-Secret");

			// If secret doesn't match, block immediately with 403
			if (expectedSecret != null && !expectedSecret.equals(incomingSecret)) {
				response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied: Incorrect Origin");
				return;
			}

			filterChain.doFilter(request, response);
		}
	}

	@Bean
	public JwtDecoder jwtDecoder() {
		SecretKeySpec secretKey = new SecretKeySpec(jwtSecret.getBytes(), "HmacSHA256");
		NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withSecretKey(secretKey).build();

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
		// NOTE: Update this list to include your production frontend domain!
		configuration.setAllowedOrigins(
				Arrays.asList("http://localhost:8081", "http://127.0.0.1:8081", "https://useoptimal.app"));
		configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS")); // Added OPTIONS for
		configuration.setAllowedHeaders(Arrays.asList("*"));
		configuration.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}
}