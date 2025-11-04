package com.example.pbanking.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AccountConsentResponse(String status, String request_id, 
                                    @JsonProperty(required = false) String consent_id, 
                                    Boolean auto_approved, String created_at) { }
