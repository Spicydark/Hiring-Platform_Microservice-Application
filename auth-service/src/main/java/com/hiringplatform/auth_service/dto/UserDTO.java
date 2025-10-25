package com.hiringplatform.auth_service.dto;

/**
 * DTO for user information excluding sensitive data like passwords.
 * Used for inter-service communication and API responses.
 */
public class UserDTO {
    private String id;
    private String username;
    private String email;
    private String role;

    public UserDTO() {}

    public UserDTO(String id, String username, String email, String role) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
