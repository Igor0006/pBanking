package com.example.pbanking.dto;

/**
 * Projection DTO representing a bank/client pair stored for a user.
 */
public record BankClientLink(String bankId, String clientId) {}
