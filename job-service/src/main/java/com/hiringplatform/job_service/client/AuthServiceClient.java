package com.hiringplatform.job_service.client;

import com.hiringplatform.job_service.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Feign client interface for communicating with the Auth Service.
 * The 'name' attribute matches the 'spring.application.name' of the auth-service
 * registered in Eureka.
 */
@FeignClient(name = "auth-service") // Name registered in Eureka
public interface AuthServiceClient {

    /**
     * Declares a method to call the endpoint in Auth Service that retrieves user details by ID.
     * Assumes Auth Service exposes: GET /users/{id}
     *
     * @param id The ID of the user to fetch.
     * @return ResponseEntity containing the UserDTO.
     */
    @GetMapping("/users/{id}") // Path relative to the Auth Service base URL
    ResponseEntity<UserDTO> getUserById(@PathVariable("id") String id);

    // Add other methods here if you need to call other endpoints in Auth Service
}
