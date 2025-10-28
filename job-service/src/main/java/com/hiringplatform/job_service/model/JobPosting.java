package com.hiringplatform.job_service.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

/**
 * Job posting entity stored in MongoDB.
 * Contains job details including role, requirements, and recruiter information.
 */
@Document(collection = "JobPostings")
public class JobPosting {

    @Id
    private String id;

    private String role;
    private String description;
    private int experience;
    private List<String> skillSet;
    private String recruiterId;

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

    /**
     * Returns string representation of job posting for debugging.
     * @return String containing all job posting fields
     */
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
