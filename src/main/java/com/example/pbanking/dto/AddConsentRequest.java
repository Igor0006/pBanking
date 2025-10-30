package com.example.pbanking.dto;

import java.util.UUID;

import com.example.pbanking.dto.enums.Bank;

// Test request
public record AddConsentRequest(
    String consent,
    Bank bank,
    UUID userId
) {
    
}
