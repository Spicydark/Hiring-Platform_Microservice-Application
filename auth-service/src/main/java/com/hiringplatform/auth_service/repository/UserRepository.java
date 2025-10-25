package com.hiringplatform.auth_service.repository;

import com.hiringplatform.auth_service.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * MongoDB repository for User entity operations.
 * Provides CRUD operations and custom queries for user data.
 */
@Repository
public interface UserRepository extends MongoRepository<User, String> {

    /**
     * Finds user by username for authentication.
     * @param username Username to search
     * @return Optional containing user if found
     */
    Optional<User> findByUsername(String username);

}
