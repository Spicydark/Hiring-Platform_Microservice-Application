package com.hiringplatform.candidate_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for Candidate Service.
 * Manages candidate profile creation, updates, and retrieval.
 */
@SpringBootApplication
public class CandidateServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CandidateServiceApplication.class, args);
	}

}
