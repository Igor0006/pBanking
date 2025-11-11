package com.example.pbanking.account.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record BalanceResponse(Data data) {
    public record Data(List<Balance> balance) {
    }

    public record Balance(
            String accountId,
            String type,
            String dateTime,
            Amount amount,
            String creditDebitIndicator) {
    }

    public record Amount(
            BigDecimal amount,
            String currency) {
    }
}
