package com.hiringplatform.candidate_service.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed; // Import Indexed
import java.util.List;

/**
 * Represents a candidate's professional profile document.
 * Stored in the "CandidateProfiles" collection.
 */
@Document(collection = "CandidateProfiles")
public class CandidateProfile {

    @Id
    private String id; // Unique MongoDB identifier.

    @Indexed(unique = true) // Ensure one profile per user
    private String userId; // ID linking this profile to a User in the Auth Service.
    private String fullName; // Candidate's full name.
    private String email; // Candidate's contact email.
    private int totalExperience; // Candidate's total years of experience.
    private List<String> skills; // List of candidate's skills.
    private String resumeUrl; // URL to the candidate's resume file.

    // --- Standard Getters and Setters ---

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getTotalExperience() {
        return totalExperience;
    }

    public void setTotalExperience(int totalExperience) {
        this.totalExperience = totalExperience;
    }

    public List<String> getSkills() {
        return skills;
    }

    public void setSkills(List<String> skills) {
        this.skills = skills;
    }

    public String getResumeUrl() {
        return resumeUrl;
    }

    public void setResumeUrl(String resumeUrl) {
        this.resumeUrl = resumeUrl;
    }
}
