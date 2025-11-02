package com.example.pbanking.dto;

public record AuthResponse(
        String token,
        long expiresIn) {
}
