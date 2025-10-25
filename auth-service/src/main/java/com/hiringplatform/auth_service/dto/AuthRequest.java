package com.hiringplatform.auth_service.dto;

/**
 * Data Transfer Object (DTO) representing the JSON body expected for a login request.
 * Contains the username and password provided by the user during login.
 */
public class AuthRequest {
    private String username;
    private String password;

    // --- Standard Getters and Setters ---

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}