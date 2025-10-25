package com.hiringplatform.candidate_service.repository;

import com.hiringplatform.candidate_service.model.CandidateProfile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * MongoDB repository for CandidateProfile entity operations.
 * Provides CRUD operations and custom queries for candidate profiles.
 */
@Repository
public interface CandidateProfileRepository extends MongoRepository<CandidateProfile, String> {

    /**
     * Finds candidate profile by associated user ID.
     * @param userId User ID from auth service
     * @return Optional containing profile if found
     */
    Optional<CandidateProfile> findByUserId(String userId);

}