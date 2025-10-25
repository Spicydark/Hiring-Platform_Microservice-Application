package com.hiringplatform.job_service.client;

import com.hiringplatform.job_service.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Feign client for Auth Service communication via Eureka.
 * Retrieves user information for inter-service operations.
 */
@FeignClient(name = "auth-service")
public interface AuthServiceClient {

    /**
     * Fetches user details by user ID.
     * @param id User ID
     * @return ResponseEntity containing UserDTO
     */
    @GetMapping("/users/{id}")
    ResponseEntity<UserDTO> getUserById(@PathVariable("id") String id);

}
