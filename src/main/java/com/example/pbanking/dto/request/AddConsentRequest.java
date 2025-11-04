package com.example.pbanking.dto.request;

import java.util.UUID;

// Test request
public record AddConsentRequest(
    String consent,
    String bank,
    UUID userId
) {
    
}
