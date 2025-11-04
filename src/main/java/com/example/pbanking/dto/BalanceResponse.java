package com.example.pbanking.dto;

import java.util.List;

public record BalanceResponse(
    Data data
) {
    public String amount() {
        return data.balance.get(0).amount.amount;
    }

    public record Data (
         List<Account> balance
    ) {}

    public record Account (
        String accountId,
        String type,
        String dateTime,
        Amount amount,
        String creditDebitIndicator

    ) {}

    public record Amount (
        String amount,
        String currency
    ) {}
}
