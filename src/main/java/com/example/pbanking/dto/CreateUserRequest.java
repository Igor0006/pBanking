package com.example.pbanking.dto;

public record CreateUserRequest(
        String username,
        String password) {
}
