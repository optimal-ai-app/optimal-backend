package com.optimal.backend.springboot;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableJpaRepositories
@EnableAspectJAutoProxy
@EnableRetry
public class Optimal {

	public static void main(String[] args) {
		// Load .env file before Spring Boot starts
		loadEnvFile();

		SpringApplication.run(Optimal.class, args);
	}

	private static void loadEnvFile() {
		try {
			Dotenv dotenv = Dotenv.configure()
					.ignoreIfMissing() // Don't fail if .env doesn't exist
					.ignoreIfMalformed() // Don't fail on malformed entries
					.load();
			// Set each env var as a system property so Spring Boot can access them
			dotenv.entries().forEach(entry -> {
				// Only set if not already defined (respects existing env vars)
				if (System.getProperty(entry.getKey()) == null && System.getenv(entry.getKey()) == null) {
					System.setProperty(entry.getKey(), entry.getValue());
				}
			});
		} catch (Exception e) {
		}
	}

}
