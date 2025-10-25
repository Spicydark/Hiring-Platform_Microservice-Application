package com.hiringplatform.candidate_service.controller;

import com.hiringplatform.candidate_service.model.CandidateProfile;
import com.hiringplatform.candidate_service.repository.CandidateProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * REST controller for candidate profile operations.
 * Handles profile creation, updates, and retrieval by profile or user ID.
 */
@RestController
@RequestMapping("/candidate")
@CrossOrigin(origins = "*", maxAge = 3600)
public class CandidateController {

    @Autowired
    private CandidateProfileRepository candidateProfileRepository;

    /**
     * Creates or updates candidate profile for authenticated user.
     * @param profile Candidate profile data
     * @param userId User ID from gateway header
     * @return Saved or updated profile
     */
    @PostMapping("/profile")
    public ResponseEntity<CandidateProfile> saveOrUpdateProfile(
            @RequestBody CandidateProfile profile,
            @RequestHeader("X-User-ID") String userId) {

        try {
            Optional<CandidateProfile> existingProfileOpt = candidateProfileRepository.findByUserId(userId);

            if (existingProfileOpt.isPresent()) {
                CandidateProfile existingProfile = existingProfileOpt.get();
                existingProfile.setFullName(profile.getFullName());
                existingProfile.setEmail(profile.getEmail());
                existingProfile.setTotalExperience(profile.getTotalExperience());
                existingProfile.setSkills(profile.getSkills());
                existingProfile.setResumeUrl(profile.getResumeUrl());
                CandidateProfile updatedProfile = candidateProfileRepository.save(existingProfile);
                return ResponseEntity.ok(updatedProfile);
            } else {
                profile.setUserId(userId);
                profile.setId(null);
                CandidateProfile savedProfile = candidateProfileRepository.save(profile);
                return ResponseEntity.status(HttpStatus.CREATED).body(savedProfile);
            }
        } catch (Exception e) {
            System.err.println("Error saving/updating profile for user " + userId + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Retrieves candidate profile by profile ID.
     * @param profileId Profile document ID
     * @return Candidate profile or 404
     */
    @GetMapping("/profile/{profileId}")
    public ResponseEntity<CandidateProfile> getProfileById(@PathVariable String profileId) {
        return candidateProfileRepository.findById(profileId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Retrieves candidate profile by user ID for inter-service calls.
     * @param userId User ID from auth service
     * @return Candidate profile or 404
     */
    @GetMapping("/profile/user/{userId}")
    public ResponseEntity<CandidateProfile> getProfileByUserId(@PathVariable String userId) {
        return candidateProfileRepository.findByUserId(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
