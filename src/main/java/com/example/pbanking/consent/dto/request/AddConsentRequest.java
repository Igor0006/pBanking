package com.example.pbanking.consent.dto.request;

import java.util.UUID;

// Test request
public record AddConsentRequest(
    String consent,
    String bank,
    UUID userId
) {
    
}
