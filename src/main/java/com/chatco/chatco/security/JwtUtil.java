package com.chatco.chatco.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.convert.DurationUnit;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration:1d}")
    @DurationUnit(ChronoUnit.DAYS)
    private Duration expiration;

    @PostConstruct
    void validate() {
        if (secret == null || secret.getBytes().length < 32) {
            throw new IllegalStateException(
                    "jwt.secret must be at least 32 characters for HMAC-SHA256");
        }
    }

    private Key key() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * Creates a signed JWT whose subject is the application username.
     */
    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration.toMillis()))
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Reads the username stored in the JWT subject claim.
     */
    public String extractUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    /**
     * Checks whether the token can be parsed and verified with the configured secret.
     */
    public boolean isValid(String token) {
        try {
            extractUsername(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
}
