package com.example.pbanking.dto;

public record AccountConsentResponse(String status, String consent_id, Boolean auto_approved) { }
