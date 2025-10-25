package com.hiringplatform.auth_service.repository;

import com.hiringplatform.auth_service.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data MongoDB repository for the User collection.
 * Provides standard CRUD operations and custom query methods.
 */
@Repository
public interface UserRepository extends MongoRepository<User, String> {

    /**
     * Finds a user by their unique username.
     * This method is crucial for the Spring Security authentication process.
     *
     * @param username The username to search for.
     * @return An Optional containing the User if found, otherwise empty.
     */
    Optional<User> findByUsername(String username);

}
