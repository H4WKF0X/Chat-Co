// security/JwtUtil.java
package com.chatco.chatco.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.convert.DurationUnit;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Component
/**
 * Small helper for creating and validating JWT tokens used by the REST API.
 */
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration:1d}")
    @DurationUnit(ChronoUnit.DAYS)
    private Duration expiration;

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
