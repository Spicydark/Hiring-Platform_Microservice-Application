package com.hiringplatform.job_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Main entry point for Job Service.
 * Manages job postings, applications, and coordinates with auth and candidate services.
 */
@SpringBootApplication
@EnableFeignClients(basePackages = "com.hiringplatform.job_service.client")
public class JobServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(JobServiceApplication.class, args);
	}

}
