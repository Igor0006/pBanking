package com.example.pbanking.model;

public record AccountConsentResponse(String status, String consent_id, Boolean auto_approved) { }
