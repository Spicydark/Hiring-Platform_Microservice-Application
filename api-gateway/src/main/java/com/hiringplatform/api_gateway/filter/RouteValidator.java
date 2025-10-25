package com.hiringplatform.api_gateway.filter;

import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Validates routes, checking if they are public or require specific roles.
 */
@Component
public class RouteValidator {

    // Define public endpoints (no auth required)
    public static final Set<String> publicApiEndpoints = Set.of(
            "/register",
            "/login",
            "/posts/all",
            "/posts/search/**", // Allow search subpaths
            "/posts/{id}"       // Allow getting specific job by ID (Assuming GET is public)
    );

    // Define endpoints restricted by ROLE
    // Key: Role required (without ROLE_ prefix), Value: Map<HTTPMethod, List<AntPathPattern>>
    public static final Map<String, Map<HttpMethod, List<String>>> roleSpecificEndpoints = Map.of(
            "RECRUITER", Map.of(
                    HttpMethod.POST, List.of("/posts/add") // Only Recruiters can POST to /posts/add
            ),
            "JOB_SEEKER", Map.of(
                    HttpMethod.POST, List.of(
                        "/posts/apply/**",   // Only Job Seekers can apply
                        "/candidate/profile" // Only Job Seekers can create/update profile
                    )
            )
            // Add other roles/methods if needed
    );

     // Define endpoints requiring authentication but accessible by ANY role
     public static final Map<HttpMethod, List<String>> authenticatedEndpoints = Map.of(
        HttpMethod.GET, List.of(
             "/candidate/profile/**" // E.g., GET /candidate/profile/{userId}
            // Add other GET endpoints here that require login but not a specific role
        )
        // Add POST, PUT etc. if needed
     );


    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    /**
     * Checks if the request URI path/method matches any public endpoints.
     * @param request The incoming server request.
     * @return true if the path is public, false otherwise.
     */
     public boolean isPublic(ServerHttpRequest request) {
        String path = request.getURI().getPath();
        HttpMethod method = request.getMethod();

        // Allow OPTIONS requests (CORS preflight) globally - adjust if needed
        if (method == HttpMethod.OPTIONS) {
            return true;
        }

        // Check against public GET endpoints patterns (more specific check)
        return publicApiEndpoints.stream().anyMatch(
                pattern -> pathMatcher.match(pattern, path) &&
                           (method == HttpMethod.GET || pattern.equals("/register") || pattern.equals("/login")) // Allow GET + register/login POST
        );
    }

    /**
     * Checks if the user with the given roles is authorized for the requested path and method.
     * Assumes the path is NOT public.
     * @param request The incoming server request.
     * @param userRoles List of roles extracted from the JWT (e.g., ["ROLE_RECRUITER"]).
     * @return true if authorized, false otherwise.
     */
    public boolean isAuthorized(ServerHttpRequest request, List<String> userRoles) {
        if (userRoles == null || userRoles.isEmpty()) {
            return false; // No roles, cannot access secured endpoints
        }

        String requestPath = request.getURI().getPath();
        HttpMethod requestMethod = request.getMethod();

        // Check if the endpoint requires ANY authenticated user for the given method
        List<String> allowedForAnyAuth = authenticatedEndpoints.get(requestMethod);
        if (allowedForAnyAuth != null && allowedForAnyAuth.stream().anyMatch(pattern -> pathMatcher.match(pattern, requestPath))) {
            return true; // Any logged-in user is allowed for this path and method
        }

        // Check role-specific paths for the given method
        for (String roleWithPrefix : userRoles) {
            String simpleRole = roleWithPrefix.replace("ROLE_", ""); // Remove prefix if present
            Map<HttpMethod, List<String>> methodToPathsMap = roleSpecificEndpoints.get(simpleRole);

            if (methodToPathsMap != null) {
                List<String> allowedPaths = methodToPathsMap.get(requestMethod);
                 if (allowedPaths != null && allowedPaths.stream().anyMatch(pattern -> pathMatcher.match(pattern, requestPath))) {
                    return true; // Role matches an allowed path for the specific method
                 }
            }
        }

        System.err.println("Authorization Failed: User roles " + userRoles + " not permitted for " + requestMethod + " " + requestPath);
        return false; // No matching role/method/path found
    }
}

