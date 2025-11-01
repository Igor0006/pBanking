package com.example.pbanking.dto;


import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.List;

public record AccountsResponse(
        Data data,
        Links links,
        Meta meta) 
{
    public List<Account> accounts() {
        return data == null || data.account() == null ? List.of() : data.account();
    }

    public record Data(
            List<Account> account) {
    }

    public record Links(
            String self) {
    }

    public record Meta(
            Integer totalPages) {
    }

    public record Account(
            String accountId,
            String status, // can be replased for enum
            String currency,
            String accountType, // Business / Personal / ...
            String accountSubType, // Savings / Checking / ...
            String nickname,
            LocalDate openingDate,
            @JsonProperty("account") List<AccountReference> accountReferences) {
    }

    public record AccountReference(
            String schemeName,
            String identification,
            String name) {
    }
}