package com.example.pbanking.dto.request;

public record AuthUserRequest(
        String username,
        String password) {
}
