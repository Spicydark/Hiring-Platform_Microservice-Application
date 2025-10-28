package com.hiringplatform.auth_service.controller;

import com.hiringplatform.auth_service.dto.AuthRequest;
import com.hiringplatform.auth_service.dto.UserDTO;
import com.hiringplatform.auth_service.model.User;
import com.hiringplatform.auth_service.repository.UserRepository;
import com.hiringplatform.auth_service.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * REST controller for user authentication and registration endpoints.
 * Provides operations for user registration, login, and user retrieval.
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
     * Registers a new user with encoded password.
     * @param user User object with username, password, email, and role
     * @return Success message or error if username already exists
     */
    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody User user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Error: Username is already taken!");
        }
         if (user.getEmail() == null || user.getEmail().isEmpty()) {
             return ResponseEntity.badRequest().body("Error: Email is required!");
         }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return ResponseEntity.ok("User registered successfully!");
    }

    /**
     * Authenticates user credentials and generates JWT token.
     * @param authRequest Login credentials with username and password
     * @return JWT token string on success or error message on failure
     */
    @PostMapping("/login")
    public ResponseEntity<String> loginUser(@RequestBody AuthRequest authRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
            );
            final UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            Optional<com.hiringplatform.auth_service.model.User> userOptional = userRepository.findByUsername(userDetails.getUsername());
            if (!userOptional.isPresent()) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Authenticated user record not found");
            }
            String userId = userOptional.get().getId();
            final String jwt = jwtUtil.generateToken(userDetails, userId);
            return ResponseEntity.ok(jwt);
        } catch (Exception e) {
             return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
    }

    /**
     * Retrieves user details by user ID for inter-service communication.
     * @param userId User ID to look up
     * @return UserDTO with user details or 404 if not found
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable("id") String userId) {
         Optional<User> userOptional = userRepository.findById(userId);
         if (userOptional.isPresent()) {
             User user = userOptional.get();
             UserDTO userDTO = new UserDTO(user.getId(), user.getUsername(), user.getEmail(), user.getRole());
             return ResponseEntity.ok(userDTO);
         } else {
             return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
         }
    }
}