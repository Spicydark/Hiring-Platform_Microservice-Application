package com.hiringplatform.api_gateway.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

/**
 * Utility class for handling JWT operations within the API Gateway.
 * Reads the secret key from application properties.
 */
@Component
public class JwtUtil {

    private SecretKey secretKey;

    // Inject the Base64 encoded secret key from application.properties
    @Value("${jwt.secret}")
    public void setSecret(String base64Secret) {
        byte[] keyBytes = Base64.getDecoder().decode(base64Secret);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Example: Extract roles (adjust claim name based on your Auth Service token generation)
    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        final Claims claims = extractAllClaims(token);
        // Adjust the claim name ("roles", "authorities", etc.) based on how Auth Service creates the token
        List<String> roles = claims.get("roles", List.class); // Or "authorities"
        if (roles == null) {
             roles = claims.get("authorities", List.class); // Try alternative common claim name
        }
        return roles;
    }


    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                   .setSigningKey(secretKey)
                   .build()
                   .parseClaimsJws(token)
                   .getBody();
    }

    private Boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (Exception e) {
            // Consider logging the exception, e.g., SignatureException, ExpiredJwtException
            System.err.println("Error checking token expiration: " + e.getMessage());
            return true; // Treat errors during expiration check as expired
        }
    }

    /**
     * Validates the token structure, signature, and expiration.
     * Does NOT check username against UserDetails here, as Gateway doesn't have UserDetailsService.
     *
     * @param token The JWT token string.
     * @return true if the token is structurally valid, signed correctly, and not expired.
     */
    public Boolean validateToken(String token) {
         try {
             // Parsing the claims implicitly validates the signature
             extractAllClaims(token);
             // Explicitly check expiration
             return !isTokenExpired(token);
         } catch (Exception e) {
             // Log different JWT exceptions if needed (SignatureException, MalformedJwtException, etc.)
             System.err.println("JWT validation failed: " + e.getMessage());
             return false;
         }
     }
}
