package com.hiringplatform.job_service.client;

import com.hiringplatform.job_service.model.CandidateProfile; // Re-use model as DTO
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Feign client interface for communicating with the Candidate Service.
 * The 'name' attribute matches the 'spring.application.name' of the candidate-service
 * registered in Eureka.
 */
@FeignClient(name = "candidate-service") // Name registered in Eureka
public interface CandidateServiceClient {

    /**
     * Declares a method to call the endpoint in Candidate Service that retrieves
     * a candidate profile by their associated User ID.
     * Assumes Candidate Service exposes: GET /candidate/profile/user/{userId}
     *
     * @param userId The ID of the user whose profile to fetch.
     * @return ResponseEntity containing the CandidateProfile.
     */
    @GetMapping("/candidate/profile/user/{userId}") // Path relative to Candidate Service
    ResponseEntity<CandidateProfile> getProfileByUserId(@PathVariable("userId") String userId);

}
