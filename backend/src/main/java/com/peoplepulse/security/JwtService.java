
package com.peoplepulse.security;

import com.peoplepulse.model.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {

    @Value("${app.jwt.secret:}")
    private String secret;

    @Value("${app.jwt.expiration-ms:3600000}")
    private long expirationMs;

    private SecretKey signingKey;

    @PostConstruct
    public void validateConfiguration() {

        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                    "APP_JWT_SECRET must be configured");
        }

        if (expirationMs <= 0) {
            throw new IllegalStateException(
                    "JWT expiration must be greater than zero");
        }

        try {
            byte[] keyBytes = Decoders.BASE64.decode(secret);

            if (keyBytes.length < 32) {
                throw new IllegalStateException(
                        "JWT secret must contain at least 256 bits");
            }

            signingKey = Keys.hmacShaKeyFor(keyBytes);

        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException(
                    "JWT secret must be a valid Base64-encoded key of at least 32 bytes",
                    ex);
        }
    }

    public String generateToken(User user) {

        Date now = new Date();

        return Jwts.builder()
                .subject(user.getEmail())
                .claim("role", user.getRole())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMs))
                .signWith(signingKey)
                .compact();
    }

    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    public boolean isTokenValid(String token, String email) {

        try {
            Claims claims = extractAllClaims(token);

            return claims.getSubject() != null
                    && claims.getSubject().equalsIgnoreCase(email)
                    && claims.getExpiration() != null
                    && claims.getExpiration().after(new Date());

        } catch (Exception ex) {
            return false;
        }
    }

    private Claims extractAllClaims(String token) {

        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
