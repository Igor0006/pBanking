package com.example.pbanking.consent.dto.response;

public record PaymentConsentResponse(
    String request_id,
    String consent_id,
    String status,
    String consent_type,
    boolean auto_approved,
    String message,
    String valid_until
) {
    
}
