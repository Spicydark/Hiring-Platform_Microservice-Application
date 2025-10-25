package com.hiringplatform.candidate_service.repository;

import com.hiringplatform.candidate_service.model.CandidateProfile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data MongoDB repository for the CandidateProfile collection.
 * Provides standard CRUD operations and custom finders.
 */
@Repository
public interface CandidateProfileRepository extends MongoRepository<CandidateProfile, String> {

    /**
     * Finds a candidate profile based on the associated user ID.
     * Used internally and potentially by other services (like Job Service)
     * to retrieve candidate details.
     *
     * @param userId The unique ID of the user (from Auth Service).
     * @return An Optional containing the CandidateProfile if found.
     */
    Optional<CandidateProfile> findByUserId(String userId);

}