package com.hiringplatform.candidate_service.controller;

import com.hiringplatform.candidate_service.model.CandidateProfile;
import com.hiringplatform.candidate_service.repository.CandidateProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * REST Controller for managing Candidate Profile operations.
 */
@RestController
@RequestMapping("/candidate") // Base path for candidate-related endpoints
@CrossOrigin(origins = "*", maxAge = 3600) // Allow requests from gateway/frontend
public class CandidateController {

    @Autowired
    private CandidateProfileRepository candidateProfileRepository;

    /**
     * Creates or updates the profile for a specific user.
     * Endpoint: POST /candidate/profile
     * Expects the user's ID in the 'X-User-ID' header.
     *
     * @param profile The candidate profile data from the request body.
     * @param userId  The ID of the user creating/updating the profile (from header).
     * @return ResponseEntity containing the saved/updated profile or an error.
     */
    @PostMapping("/profile")
    public ResponseEntity<CandidateProfile> saveOrUpdateProfile(
            @RequestBody CandidateProfile profile,
            @RequestHeader("X-User-ID") String userId) { // Get user ID from header

        try {
            // Check if a profile already exists for this user
            Optional<CandidateProfile> existingProfileOpt = candidateProfileRepository.findByUserId(userId);

            if (existingProfileOpt.isPresent()) {
                // Update existing profile
                CandidateProfile existingProfile = existingProfileOpt.get();
                existingProfile.setFullName(profile.getFullName());
                existingProfile.setEmail(profile.getEmail());
                existingProfile.setTotalExperience(profile.getTotalExperience());
                existingProfile.setSkills(profile.getSkills());
                existingProfile.setResumeUrl(profile.getResumeUrl());
                // ID and UserId remain the same
                CandidateProfile updatedProfile = candidateProfileRepository.save(existingProfile);
                return ResponseEntity.ok(updatedProfile);
            } else {
                // Create new profile
                profile.setUserId(userId); // Link profile to the user
                profile.setId(null); // Ensure MongoDB generates a new ID
                CandidateProfile savedProfile = candidateProfileRepository.save(profile);
                return ResponseEntity.status(HttpStatus.CREATED).body(savedProfile);
            }
        } catch (Exception e) {
            // Log the exception in a real application
            System.err.println("Error saving/updating profile for user " + userId + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Retrieves a candidate's profile using their unique profile ID.
     * Endpoint: GET /candidate/profile/{profileId}
     *
     * @param profileId The unique ID of the CandidateProfile document.
     * @return ResponseEntity containing the profile if found, or 404 Not Found.
     */
    @GetMapping("/profile/{profileId}")
    public ResponseEntity<CandidateProfile> getProfileById(@PathVariable String profileId) {
        return candidateProfileRepository.findById(profileId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Retrieves a candidate's profile using their associated User ID.
     * Endpoint: GET /candidate/profile/user/{userId}
     * This is the endpoint the Job Service will call during the application process.
     *
     * @param userId The ID of the user (from Auth Service).
     * @return ResponseEntity containing the profile if found, or 404 Not Found.
     */
    @GetMapping("/profile/user/{userId}")
    public ResponseEntity<CandidateProfile> getProfileByUserId(@PathVariable String userId) {
        return candidateProfileRepository.findByUserId(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
