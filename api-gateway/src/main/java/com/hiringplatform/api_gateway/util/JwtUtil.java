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
 * JWT utility for API Gateway token validation and claim extraction.
 * Validates tokens without UserDetailsService, checks signature and expiration.
 */
@Component
public class JwtUtil {

    private SecretKey secretKey;

    @Value("${jwt.secret}")
    public void setSecret(String base64Secret) {
        byte[] keyBytes = Base64.getDecoder().decode(base64Secret);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Extracts username from JWT token.
     * @param token JWT string
     * @return Username claim
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extracts expiration date from JWT token.
     * @param token JWT string
     * @return Expiration date
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extracts user roles from JWT token.
     * @param token JWT string
     * @return List of role strings
     */
    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        final Claims claims = extractAllClaims(token);
        List<String> roles = claims.get("roles", List.class);
        if (roles == null) {
             roles = claims.get("authorities", List.class);
        }
        return roles;
    }

    /**
     * Extracts specific claim using resolver function.
     * @param token JWT string
     * @param claimsResolver Function to extract claim
     * @param <T> Type of claim
     * @return Extracted claim value
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Parses and extracts all claims from token.
     * @param token JWT string
     * @return Claims object
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                   .setSigningKey(secretKey)
                   .build()
                   .parseClaimsJws(token)
                   .getBody();
    }

    /**
     * Checks if token is expired.
     * @param token JWT string
     * @return True if expired, false otherwise
     */
    private Boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (Exception e) {
            System.err.println("Error checking token expiration: " + e.getMessage());
            return true;
        }
    }

    /**
     * Validates token signature and expiration.
     * @param token JWT string
     * @return True if valid, false otherwise
     */
    public Boolean validateToken(String token) {
         try {
             extractAllClaims(token);
             return !isTokenExpired(token);
         } catch (Exception e) {
             System.err.println("JWT validation failed: " + e.getMessage());
             return false;
         }
     }
}
