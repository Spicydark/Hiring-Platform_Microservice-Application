package com.hiringplatform.auth_service.filter;

import com.hiringplatform.auth_service.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * A servlet filter that intercepts incoming requests once per request.
 * It checks for a JWT in the 'Authorization' header, validates it,
 * and sets the user's authentication context if the token is valid.
 */
@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService userDetailsService; // Provided by SecurityConfig

    /**
     * Processes each incoming HTTP request.
     * Extracts JWT, validates it, and sets the SecurityContext.
     *
     * @param request     The incoming HttpServletRequest.
     * @param response    The outgoing HttpServletResponse.
     * @param filterChain The filter chain to pass the request along.
     * @throws ServletException If a servlet-specific error occurs.
     * @throws IOException      If an I/O error occurs.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;

        // 1. Extract the token from the "Authorization: Bearer <token>" header
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            try {
                // 2. Extract username from the token's subject claim
                username = jwtUtil.extractUsername(jwt);
            } catch (Exception e) {
                // Log the exception (e.g., token expired, malformed)
                System.err.println("JWT processing error: " + e.getMessage());
                // Consider adding proper logging here
            }
        }

        // 3. If username is extracted and no authentication is currently set in the context
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // 4. Load user details from the database using the username from the token
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            // 5. Validate the token (checks signature, expiration, and username match)
            if (jwtUtil.validateToken(jwt, userDetails)) {

                // 6. Create an authentication token (principal, credentials=null, authorities)
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());

                // 7. Set additional details (like IP address, session ID) from the request
                usernamePasswordAuthenticationToken
                        .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 8. Set the authentication object in the Spring Security context
                // This effectively authenticates the user for this request
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            }
        }
        // 9. Continue the filter chain, passing the request to the next filter or the controller
        filterChain.doFilter(request, response);
    }
}
