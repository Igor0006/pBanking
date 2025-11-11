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

// "request_id": "pcr-b7285a28875b",
// "consent_id": "pcon-5d62ae0a52a1",
// "status": "approved",
// "consent_type": "single_use",
// "auto_approved": true,
// "message": "Согласие одобрено автоматически",
// "valid_until": "2026-01-31T16:45:28.800563Z"

