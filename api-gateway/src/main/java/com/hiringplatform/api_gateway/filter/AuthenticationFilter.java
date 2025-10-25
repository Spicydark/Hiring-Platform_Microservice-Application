package com.hiringplatform.api_gateway.filter;

import com.hiringplatform.api_gateway.util.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Custom Gateway Filter to:
 * 1. Bypass checks for public routes (defined in RouteValidator).
 * 2. Validate JWT tokens for secured routes.
 * 3. Perform role-based authorization checks (using RouteValidator).
 * 4. Add custom headers (X-User-ID, X-User-Roles) for downstream services.
 */
@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    @Autowired
    private RouteValidator validator; // Validator checks public paths and roles

    @Autowired
    private JwtUtil jwtUtil;

    public AuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            // 1. Check if the endpoint is public using the validator
            if (validator.isPublic(request)) {
                return chain.filter(exchange); // Public route, skip all auth checks
            }

            // --- It's a secured route, proceed with JWT checks ---

            // 2. Check if Authorization header is present
            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                return onError(exchange, HttpStatus.UNAUTHORIZED, "Authorization header is missing");
            }

            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return onError(exchange, HttpStatus.UNAUTHORIZED, "Invalid Authorization header format (Requires Bearer token)");
            }

            // 3. Extract Token
            String token = authHeader.substring(7);

            try {
                // 4. Validate Token (Signature & Expiration)
                if (!jwtUtil.validateToken(token)) {
                     // The validateToken method handles internal exceptions and returns false if invalid
                     return onError(exchange, HttpStatus.UNAUTHORIZED, "JWT Token is invalid or expired");
                }

                // 5. Extract Claims (Username and Roles)
                String username = jwtUtil.extractUsername(token);
                List<String> roles = jwtUtil.extractRoles(token); // Ensure roles claim name matches Auth Service

                // 6. Perform Authorization Check using RouteValidator
                if (!validator.isAuthorized(request, roles)) {
                   return onError(exchange, HttpStatus.FORBIDDEN, "Access Denied: User does not have the required role for this resource");
                }


                // 7. Add Headers for Downstream Services (If validation and authorization passed)
                ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                        .header("X-User-ID", username) // Using username as ID for simplicity, adjust if needed
                        .header("X-User-Roles", roles != null ? String.join(",", roles) : "")
                        .build();

                // Proceed with the modified request containing the new headers
                return chain.filter(exchange.mutate().request(modifiedRequest).build());

            } catch (ExpiredJwtException e) {
                 System.err.println("JWT expired: " + e.getMessage());
                 return onError(exchange, HttpStatus.UNAUTHORIZED, "JWT Token has expired");
            } catch (SignatureException | MalformedJwtException e) {
                 System.err.println("JWT signature/format error: " + e.getMessage());
                 return onError(exchange, HttpStatus.UNAUTHORIZED, "JWT Token is invalid");
            } catch (Exception e) {
                System.err.println("Unexpected error processing JWT: " + e.getMessage());
                return onError(exchange, HttpStatus.INTERNAL_SERVER_ERROR, "Error processing JWT token");
            }
        };
    }

    /**
     * Helper method to generate an error response.
     * Sets the HTTP status code and completes the response.
     *
     * @param exchange The current server exchange.
     * @param status   The HTTP status to return.
     * @param message  The error message to log.
     * @return A Mono<Void> indicating completion.
     */
    private Mono<Void> onError(ServerWebExchange exchange, HttpStatus status, String message) {
        System.err.println("API Gateway Auth Filter Error: Status=" + status + ", Message=" + message + ", Path=" + exchange.getRequest().getURI().getPath()); // Log error details
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        // Optionally add error message to response body if desired for client feedback
        // response.getHeaders().add("Content-Type", "application/json");
        // byte[] bytes = ("{\"error\":\"" + status.getReasonPhrase() + "\", \"message\":\"" + message + "\"}").getBytes(StandardCharsets.UTF_8);
        // DataBuffer buffer = response.bufferFactory().wrap(bytes);
        // return response.writeWith(Mono.just(buffer));
        return response.setComplete();
    }

    // Empty config class required by AbstractGatewayFilterFactory
    public static class Config {}
}

