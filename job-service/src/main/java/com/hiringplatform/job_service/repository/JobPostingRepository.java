package com.hiringplatform.job_service.repository;

import com.hiringplatform.job_service.model.JobPosting;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data MongoDB repository for the JobPosting collection.
 * Provides standard CRUD operations automatically.
 */
@Repository
public interface JobPostingRepository extends MongoRepository<JobPosting, String> {
    // No custom methods needed for basic CRUD.
    // Search functionality is handled by SearchRepository.
}
