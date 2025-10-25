package com.hiringplatform.job_service.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

/**
 * Represents a single job posting document in the "JobPostings" collection.
 */
@Document(collection = "JobPostings")
public class JobPosting {

    @Id
    private String id; // Unique MongoDB identifier.

    private String role; // The job title or role.
    private String description; // Detailed description of the job.
    private int experience; // Required experience in years.
    private List<String> skillSet; // List of required skills.
    private String recruiterId; // ID of the User (recruiter) who posted this job. Links to the User in Auth Service.

    // --- Standard Getters and Setters ---

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getExperience() {
        return experience;
    }

    public void setExperience(int experience) {
        this.experience = experience;
    }

    public List<String> getSkillSet() {
        return skillSet;
    }

    public void setSkillSet(List<String> skillSet) {
        this.skillSet = skillSet;
    }

     public String getRecruiterId() {
        return recruiterId;
    }

    public void setRecruiterId(String recruiterId) {
        this.recruiterId = recruiterId;
    }

    @Override
    public String toString() {
        return "JobPosting{" +
               "id='" + id + '\'' +
               ", role='" + role + '\'' +
               ", description='" + description + '\'' +
               ", experience=" + experience +
               ", skillSet=" + skillSet +
               ", recruiterId='" + recruiterId + '\'' +
               '}';
    }
}
