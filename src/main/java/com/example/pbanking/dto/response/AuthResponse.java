package com.example.pbanking.dto.response;

public record AuthResponse(
        String token,
        long expiresIn) {
}
