package com.example.pbanking.dto.request;

import java.math.BigDecimal;
import java.util.Optional;

public record MakePaymentRequest (
    String accountId,
    String debtor_account,
    String creditor_account,
    String debtor_bank,
    Optional<String> creditor_bank,
    BigDecimal amount,
    String currency,
    String comment,
    String debtor_scheme,
    String creditor_scheme
) {
    
}
