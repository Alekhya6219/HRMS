
package com.peoplepulse.security;

import com.peoplepulse.model.User;
import com.peoplepulse.repo.UserRepository;

import io.jsonwebtoken.JwtException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            UserRepository userRepository) {

        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        // No token: continue. Protected endpoints will require authentication.
        if (authHeader == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // Reject malformed or unsupported Authorization headers.
        if (!authHeader.startsWith("Bearer ")
                || authHeader.substring(7).isBlank()) {

            rejectAuthentication(response);
            return;
        }

        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7).trim();

        try {
            String email = jwtService.extractEmail(token);

            User user = userRepository
                    .findByEmailIgnoreCase(email)
                    .orElse(null);

            // Reject missing, disabled or invalid accounts/tokens.
            if (user == null
                    || !user.isEnabled()
                    || !jwtService.isTokenValid(token, user.getEmail())) {

                rejectAuthentication(response);
                return;
            }

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            user.getEmail(),
                            null,
                            List.of(
                                    new SimpleGrantedAuthority(
                                            "ROLE_" + user.getRole())
                            )
                    );

            SecurityContextHolder.getContext()
                    .setAuthentication(authentication);

        } catch (JwtException | IllegalArgumentException ex) {

            rejectAuthentication(response);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void rejectAuthentication(HttpServletResponse response)
            throws IOException {

        SecurityContextHolder.clearContext();

        response.sendError(
                HttpServletResponse.SC_UNAUTHORIZED,
                "Invalid or expired authentication token"
        );
    }
}
