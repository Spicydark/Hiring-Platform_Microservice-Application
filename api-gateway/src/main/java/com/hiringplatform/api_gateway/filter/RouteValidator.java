package com.hiringplatform.api_gateway.filter;

import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Validates route access based on public endpoints and user roles.
 * Determines which routes require authentication and specific role permissions.
 */
@Component
public class RouteValidator {

    public static final Set<String> publicApiEndpoints = Set.of(
            "/register",
            "/login",
            "/posts/all",
            "/posts/search/**",
            "/posts/{id}"
    );

    public static final Map<String, Map<HttpMethod, List<String>>> roleSpecificEndpoints = Map.of(
            "RECRUITER", Map.of(
                    HttpMethod.POST, List.of("/posts/add")
            ),
            "JOB_SEEKER", Map.of(
                    HttpMethod.POST, List.of(
                        "/posts/apply/**",
                        "/candidate/profile"
                    )
            )
    );

     public static final Map<HttpMethod, List<String>> authenticatedEndpoints = Map.of(
        HttpMethod.GET, List.of(
             "/candidate/profile/**"
        )
     );

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    /**
     * Checks if request matches public endpoints requiring no authentication.
     * @param request Server HTTP request
     * @return True if route is public, false otherwise
     */
     public boolean isPublic(ServerHttpRequest request) {
        String path = request.getURI().getPath();
        HttpMethod method = request.getMethod();

        if (method == HttpMethod.OPTIONS) {
            return true;
        }

        return publicApiEndpoints.stream().anyMatch(
                pattern -> pathMatcher.match(pattern, path) &&
                           (method == HttpMethod.GET || pattern.equals("/register") || pattern.equals("/login"))
        );
    }

    /**
     * Checks if user roles authorize access to requested path and method.
     * @param request Server HTTP request
     * @param userRoles List of user roles from JWT
     * @return True if authorized, false otherwise
     */
    public boolean isAuthorized(ServerHttpRequest request, List<String> userRoles) {
        if (userRoles == null || userRoles.isEmpty()) {
            return false;
        }
        String requestPath = request.getURI().getPath();
        HttpMethod requestMethod = request.getMethod();
        List<String> allowedForAnyAuth = authenticatedEndpoints.get(requestMethod);
        if (allowedForAnyAuth != null && allowedForAnyAuth.stream().anyMatch(pattern -> pathMatcher.match(pattern, requestPath))) {
            return true;
        }
        for (String roleWithPrefix : userRoles) {
            String simpleRole = roleWithPrefix.replace("ROLE_", "");
            Map<HttpMethod, List<String>> methodToPathsMap = roleSpecificEndpoints.get(simpleRole);
            if (methodToPathsMap != null) {
                List<String> allowedPaths = methodToPathsMap.get(requestMethod);
                 if (allowedPaths != null && allowedPaths.stream().anyMatch(pattern -> pathMatcher.match(pattern, requestPath))) {
                    return true;
                 }
            }
        }
        return false;
    }
}