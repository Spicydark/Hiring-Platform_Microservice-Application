package com.hiringplatform.auth_service.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Utility class for JWT token operations.
 * Handles token generation, validation, and claim extraction.
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretString;

    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 10;

    /**
     * Generates signing key from base64-encoded secret.
     * @return SecretKey for token signing and verification
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretString);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Extracts username from JWT token.
     * @param token JWT string
     * @return Username claim from token
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
     * Extracts specific claim from token using resolver function.
     * @param token JWT string
     * @param claimsResolver Function to extract specific claim
     * @param <T> Type of claim to extract
     * @return Extracted claim value
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Parses and validates JWT token, extracting all claims.
     * @param token JWT string
     * @return Claims object containing token payload
     */
    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Checks if JWT token has expired.
     * @param token JWT string
     * @return True if token is expired, false otherwise
     */
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Generates JWT token for authenticated user with roles.
     * @param userDetails Spring Security user details
     * @return Signed JWT token string
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", userDetails.getAuthorities().stream()
                                      .map(GrantedAuthority::getAuthority)
                                      .collect(Collectors.toList()));
        return createToken(claims, userDetails.getUsername());
    }

    /**
     * Creates JWT token with claims, subject, and expiration.
     * @param claims Custom claims to include in token
     * @param subject Token subject (username)
     * @return Compact JWT token string
     */
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Validates JWT token against user details.
     * @param token JWT string
     * @param userDetails User details to validate against
     * @return True if token is valid and not expired
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}
