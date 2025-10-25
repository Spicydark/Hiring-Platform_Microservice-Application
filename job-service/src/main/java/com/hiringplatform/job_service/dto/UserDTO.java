package com.hiringplatform.job_service.dto;

/**
 * Data Transfer Object (DTO) to represent User data fetched from the Auth Service.
 * Only includes fields needed by the Job Service (like email for notifications).
 */
public class UserDTO {
    private String id;
    private String username;
    private String email;
    private String role;

    // --- Getters and Setters ---
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
