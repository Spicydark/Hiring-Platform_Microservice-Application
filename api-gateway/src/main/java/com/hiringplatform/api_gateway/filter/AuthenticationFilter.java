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
 * Gateway filter for JWT authentication and authorization.
 * Validates tokens for secured routes and adds user headers for downstream services.
 */
@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    @Autowired
    private RouteValidator validator;

    @Autowired
    private JwtUtil jwtUtil;

    public AuthenticationFilter() {
        super(Config.class);
    }

    /**
     * Applies authentication and authorization logic to gateway requests.
     * @param config Configuration object (empty)
     * @return GatewayFilter implementation
     */
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            if (validator.isPublic(request)) {
                return chain.filter(exchange);
            }

            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                return onError(exchange, HttpStatus.UNAUTHORIZED, "Authorization header is missing");
            }

            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return onError(exchange, HttpStatus.UNAUTHORIZED, "Invalid Authorization header format (Requires Bearer token)");
            }

            String token = authHeader.substring(7);

            try {
                if (!jwtUtil.validateToken(token)) {
                     return onError(exchange, HttpStatus.UNAUTHORIZED, "JWT Token is invalid or expired");
                }

                String username = jwtUtil.extractUsername(token);
                List<String> roles = jwtUtil.extractRoles(token);

                if (!validator.isAuthorized(request, roles)) {
                   return onError(exchange, HttpStatus.FORBIDDEN, "Access Denied: User does not have the required role for this resource");
                }

                ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                        .header("X-User-ID", username)
                        .header("X-User-Roles", roles != null ? String.join(",", roles) : "")
                        .build();

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
     * Generates error response for authentication/authorization failures.
     * @param exchange Server exchange
     * @param status HTTP status code
     * @param message Error message
     * @return Mono indicating completion
     */
    private Mono<Void> onError(ServerWebExchange exchange, HttpStatus status, String message) {
        System.err.println("API Gateway Auth Filter Error: Status=" + status + ", Message=" + message + ", Path=" + exchange.getRequest().getURI().getPath());
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        return response.setComplete();
    }

    public static class Config {}
}