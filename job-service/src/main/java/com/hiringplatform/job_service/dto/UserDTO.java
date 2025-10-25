package com.hiringplatform.job_service.dto;

/**
 * DTO for user data from Auth Service.
 * Contains user information needed for job service operations.
 */
public class UserDTO {
    private String id;
    private String username;
    private String email;
    private String role;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
