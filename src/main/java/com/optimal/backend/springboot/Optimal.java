package com.optimal.backend.springboot;

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

		SpringApplication.run(Optimal.class, args);
	}

}
