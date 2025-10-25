package com.hiringplatform.job_service.client;

import com.hiringplatform.job_service.model.CandidateProfile;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Feign client for Candidate Service communication via Eureka.
 * Retrieves candidate profile information for job applications.
 */
@FeignClient(name = "candidate-service")
public interface CandidateServiceClient {

    /**
     * Fetches candidate profile by user ID.
     * @param userId User ID
     * @return ResponseEntity containing CandidateProfile
     */
    @GetMapping("/candidate/profile/user/{userId}")
    ResponseEntity<CandidateProfile> getProfileByUserId(@PathVariable("userId") String userId);

}
