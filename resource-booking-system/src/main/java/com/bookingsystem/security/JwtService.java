package com.bookingsystem.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import com.bookingsystem.exception.JwtConfigurationException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtService {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiration-ms}")
    private long expirationMs;

    @PostConstruct
    void validateConfiguration() {
        try {
            byte[] decodedSecret = java.util.Base64.getDecoder().decode(secret);
            if (decodedSecret.length < 32) {
                throw new JwtConfigurationException(
                    "Invalid JWT_SECRET: Base64 secret must decode to at least 32 bytes (256 bits)");
            }
        } catch (IllegalArgumentException ex) {
            throw new JwtConfigurationException(
                    "Invalid JWT_SECRET: value must be valid Base64 and decode to at least 32 bytes (256 bits)", ex);
        }
    }

    private SecretKey key() {
        // Secret must be a Base64-encoded string of at least 256 bits for HS256.
        return Keys.hmacShaKeyFor(java.util.Base64.getDecoder().decode(secret));
    }

    public String generateToken(UserDetails userDetails, String role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key())
                .compact();
    }

    public long getExpirationMs() {
        return expirationMs;
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private <T> T extractClaim(String token, Function<Claims, T> resolver) {
        return resolver.apply(extractAllClaims(token));
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(key())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
