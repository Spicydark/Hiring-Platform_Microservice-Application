package com.hiringplatform.auth_service.config;

import com.hiringplatform.auth_service.filter.JwtRequestFilter;
import com.hiringplatform.auth_service.repository.UserRepository;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority; // Correct import
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Collections;
import java.util.List;

/**
 * Spring Security configuration class for the Authentication Service.
 * Defines authentication mechanisms, authorization rules, and security filters.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private UserRepository userRepository;

    /**
     * Defines the service responsible for loading user-specific data (UserDetails).
     * It fetches user details from the UserRepository based on the username.
     *
     * @return An implementation of UserDetailsService.
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userRepository.findByUsername(username)
            // Map the User entity to Spring Security's UserDetails interface
            .map(user -> new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                // Convert the user's role string into a GrantedAuthority object
                // Spring Security requires roles to be prefixed with "ROLE_"
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole()))
            ))
            // Throw exception if user is not found, handled by Spring Security
            .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }

    /**
     * Defines the password encoder bean.
     * BCrypt is used for strong, salted password hashing.
     *
     * @return A PasswordEncoder instance.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configures the primary AuthenticationProvider (DaoAuthenticationProvider).
     * Links the UserDetailsService and PasswordEncoder to handle username/password authentication.
     *
     * @return An AuthenticationProvider instance.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Exposes the AuthenticationManager as a Bean.
     * Required by the AuthController for programmatic authentication during login.
     *
     * @param config The AuthenticationConfiguration provided by Spring Boot.
     * @return The AuthenticationManager instance.
     * @throws Exception If an error occurs retrieving the AuthenticationManager.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Defines the main security filter chain for the application.
     * Configures CSRF, CORS, session management, authorization rules, and JWT filter integration.
     *
     * @param http             The HttpSecurity object to configure.
     * @param jwtAuthFilter    The custom JWT filter to be added to the chain.
     * @return The configured SecurityFilterChain.
     * @throws Exception If an error occurs during configuration.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtRequestFilter jwtAuthFilter) throws Exception {
        http
            // Disable CSRF protection (common for stateless REST APIs)
            .csrf(csrf -> csrf.disable())
            // Enable CORS using the corsConfigurationSource bean
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            // Configure session management to be stateless (JWT handles state)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // Define authorization rules for HTTP requests
            .authorizeHttpRequests(auth -> auth
                // Allow public access to registration and login endpoints
                .requestMatchers("/register", "/login").permitAll()
                .requestMatchers(HttpMethod.GET, "/users/**").permitAll()
                // Require authentication for any other request
                .anyRequest().authenticated()
            )
            // Add the custom JWT filter before the standard UsernamePasswordAuthenticationFilter
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            // Configure custom exception handling for authentication/authorization failures
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint(authenticationEntryPoint()) // Handles failed authentication attempts (401)
                .accessDeniedHandler(accessDeniedHandler())         // Handles failed authorization attempts (403)
            );

        return http.build();
    }

    /**
     * Configures Cross-Origin Resource Sharing (CORS) settings.
     * Allows requests from specified origins (e.g., the frontend application or API Gateway).
     *
     * @return A CorsConfigurationSource instance.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // Allow requests specifically from the frontend/gateway origin during development/production
        // IMPORTANT: Replace "*" with specific origins in production for security!
        config.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:8081")); // Example: Allow frontend and gateway
        // Allow common HTTP methods
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        // Allow specific headers required for authentication and content type
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        // Allow credentials (like cookies or auth headers) to be sent
        // Note: Cannot use "*" for allowedOrigins when allowCredentials is true
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Apply this CORS configuration to all paths ("/**")
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    /**
     * Defines the entry point for handling authentication failures (401 Unauthorized).
     * Returns a simple error message in the response body.
     *
     * @return An AuthenticationEntryPoint instance.
     */
    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
            response.getWriter().write("Unauthorized: " + authException.getMessage());
        };
    }

    /**
     * Defines the handler for handling authorization failures (403 Forbidden).
     * Returns a simple error message when an authenticated user tries to access a resource they don't have permission for.
     *
     * @return An AccessDeniedHandler instance.
     */
    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403
            response.getWriter().write("Access Denied: " + accessDeniedException.getMessage());
        };
    }
}
