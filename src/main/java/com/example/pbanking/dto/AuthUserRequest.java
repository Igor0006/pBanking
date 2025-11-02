package com.example.pbanking.dto;

public record AuthUserRequest(
        String username,
        String password) {
}
