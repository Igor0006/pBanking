package com.example.pbanking.dto;

import java.util.UUID;

// Test request
public record AddConsentRequest(
    String consent,
    String bank,
    UUID userId
) {
    
}
