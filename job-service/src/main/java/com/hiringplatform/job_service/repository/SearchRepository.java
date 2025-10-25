package com.hiringplatform.job_service.repository;

import com.hiringplatform.job_service.model.JobPosting;
import java.util.List;

/**
 * Interface defining custom search operations for JobPostings.
 * Separates complex search logic from the basic CRUD repository.
 */
public interface SearchRepository {

    /**
     * Performs a text-based search across relevant fields of JobPostings.
     *
     * @param text The search query string.
     * @return A list of matching JobPosting objects.
     */
    List<JobPosting> findByText(String text);

}