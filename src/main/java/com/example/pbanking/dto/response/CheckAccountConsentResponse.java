package com.example.pbanking.dto.response;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CheckAccountConsentResponse(@JsonProperty("data") Data data) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Data(
            @JsonProperty(required = false) String consentId,
            String status,
            Instant creationDateTime,
            Instant statusUpdateDateTime,
            List<String> permissions,
            Instant expirationDateTime) {
    }
}