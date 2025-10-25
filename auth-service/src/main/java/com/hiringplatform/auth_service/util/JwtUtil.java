package com.hiringplatform.auth_service.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value; // Import Value
import org.springframework.security.core.GrantedAuthority; // For roles
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors; // For roles

/**
 * Utility class for handling JSON Web Tokens (JWT).
 * Provides methods for generating, validating, and extracting claims from tokens.
 */
@Component
public class JwtUtil {

    // Inject the secret key from application properties for better security
    @Value("${jwt.secret}")
    private String secretString;

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretString);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // Define token validity duration (e.g., 10 hours)
    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 10; // 10 hours in milliseconds

    /**
     * Extracts the username (subject) from a JWT token.
     *
     * @param token The JWT string.
     * @return The username contained within the token.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extracts the expiration date from a JWT token.
     *
     * @param token The JWT string.
     * @return The expiration date.
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * A generic function to extract a specific claim from the token's payload.
     *
     * @param token          The JWT string.
     * @param claimsResolver A function to apply to the claims.
     * @param <T>            The type of the claim to be extracted.
     * @return The extracted claim value.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Parses the JWT token and returns all claims.
     * Handles signature validation.
     *
     * @param token The JWT string.
     * @return The Claims object representing the token's payload.
     */
    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSigningKey()) // Use the dynamic key
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Checks if a JWT token has expired.
     *
     * @param token The JWT string.
     * @return True if the token is expired, false otherwise.
     */
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Generates a new JWT for the given user details.
     * Includes username as subject and roles/authorities as a custom claim.
     *
     * @param userDetails Spring Security UserDetails object representing the authenticated user.
     * @return A JWT string.
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        // Include user roles/authorities in the token
        claims.put("roles", userDetails.getAuthorities().stream()
                                      .map(GrantedAuthority::getAuthority)
                                      .collect(Collectors.toList()));
        return createToken(claims, userDetails.getUsername());
    }

    /**
     * Creates the JWT string with specified claims, subject, issued time, expiration time, and signature.
     *
     * @param claims  Custom claims to include in the payload.
     * @param subject The subject of the token (typically the username).
     * @return The compacted JWT string.
     */
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256) // Use the dynamic key
                .compact();
    }

    /**
     * Validates a JWT token against the user details.
     * Checks if the username matches and if the token is not expired.
     *
     * @param token       The JWT string.
     * @param userDetails The UserDetails object for the user identified in the token.
     * @return True if the token is valid, false otherwise.
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}
