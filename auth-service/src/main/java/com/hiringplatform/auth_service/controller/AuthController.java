package com.hiringplatform.auth_service.controller;

import com.hiringplatform.auth_service.dto.AuthRequest;
import com.hiringplatform.auth_service.dto.UserDTO; // Import UserDTO
import com.hiringplatform.auth_service.model.User;
import com.hiringplatform.auth_service.repository.UserRepository;
import com.hiringplatform.auth_service.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus; // Import HttpStatus
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*; // Import GetMapping, PathVariable

import java.util.Optional; // Import Optional

/**
 * Controller handling user registration, login, and retrieval endpoints.
 */
@RestController
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Endpoint for user registration.
     * Encodes the password before saving.
     * @param user User data from the request body.
     * @return Success or error message.
     */
    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody User user) {
        // Check if username already exists
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Error: Username is already taken!");
        }
        // Check if email is provided (assuming it's required)
         if (user.getEmail() == null || user.getEmail().isEmpty()) {
             return ResponseEntity.badRequest().body("Error: Email is required!");
         }
        // Encode password
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        // Save the new user
        userRepository.save(user);
        return ResponseEntity.ok("User registered successfully!");
    }

    /**
     * Endpoint for user login.
     * Authenticates credentials and returns a JWT upon success.
     * @param authRequest Login credentials (username, password).
     * @return JWT string or error message.
     */
    @PostMapping("/login")
    public ResponseEntity<String> loginUser(@RequestBody AuthRequest authRequest) {
        try {
            // Attempt authentication
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
            );

            // If successful, generate JWT
            final UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            final String jwt = jwtUtil.generateToken(userDetails);

            // Return the JWT
            return ResponseEntity.ok(jwt);

        } catch (Exception e) {
             // Handle authentication failure
             System.err.println("Login failed for user " + authRequest.getUsername() + ": " + e.getMessage());
             return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
    }

    /**
     * Endpoint to retrieve user details by User ID.
     * Made public in SecurityConfig for inter-service communication.
     * @param userId The ID of the user to retrieve.
     * @return UserDTO with user details or 404 if not found.
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable("id") String userId) {
         Optional<User> userOptional = userRepository.findById(userId);
         if (userOptional.isPresent()) {
             User user = userOptional.get();
             // Map to DTO (exclude password)
             UserDTO userDTO = new UserDTO(user.getId(), user.getUsername(), user.getEmail(), user.getRole());
             return ResponseEntity.ok(userDTO);
         } else {
             System.err.println("User not found for ID: " + userId); // Add logging
             return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
         }
    }
}

