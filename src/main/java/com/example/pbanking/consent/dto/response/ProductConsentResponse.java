package com.example.pbanking.consent.dto.response;

public record ProductConsentResponse (
    String request_id,
    String consent_id,
    String status,
    boolean auto_approved,
    String message,
    String valid_until
) {}
