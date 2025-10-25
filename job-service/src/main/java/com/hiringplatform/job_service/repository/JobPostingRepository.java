package com.hiringplatform.job_service.repository;

import com.hiringplatform.job_service.model.JobPosting;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * MongoDB repository for JobPosting entity operations.
 * Provides standard CRUD operations for job postings.
 */
@Repository
public interface JobPostingRepository extends MongoRepository<JobPosting, String> {
}
