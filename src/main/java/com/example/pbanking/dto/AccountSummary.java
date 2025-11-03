package com.example.pbanking.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * View model for returning account details enriched with balance.
 */
public record AccountSummary(
        String accountId,
        String status,
        String currency,
        String accountSubType,
        String nickname,
        LocalDate openingDate,
        @JsonProperty("account") List<AccountsResponse.AccountReference> account,
        BigDecimal amount) {
}
