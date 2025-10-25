package com.hiringplatform.job_service.repository;

import com.hiringplatform.job_service.model.JobPosting;
import java.util.List;

/**
 * Interface for custom job posting search operations.
 * Implemented using MongoDB aggregation for text-based searches.
 */
public interface SearchRepository {

    /**
     * Searches job postings by text query across multiple fields.
     * @param text Search query string
     * @return List of matching job postings
     */
    List<JobPosting> findByText(String text);

}