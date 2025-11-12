package com.example.pbanking.user.dto.response;

public record AuthResponse(
        String token,
        long expiresIn) {
}
