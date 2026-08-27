package com.bookingsystem.dto.auth;

public record LoginResponse(
        String token,
        String tokenType,
        String username,
        String role,
        long expiresInMs
) {
}
