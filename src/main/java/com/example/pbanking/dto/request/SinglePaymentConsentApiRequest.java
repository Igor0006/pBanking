package com.example.pbanking.dto.request;

import java.math.BigDecimal;

import lombok.Getter;

@Getter
public class SinglePaymentConsentApiRequest extends BasePaymentConsentApiRequest {
    private BigDecimal amount;
    private String currency;
    private String creditor_account;
    private String creditor_name;
    private String reference;

    public SinglePaymentConsentApiRequest(String bankId, String debtorAccount, BigDecimal amount, String currency,
            String creditor_account, String creditor_name,
            String reference) {
        super(bankId, debtorAccount);
        this.amount = amount;
        this.currency = currency;
        this.creditor_account = creditor_account;
        this.creditor_name = creditor_name;
        this.reference = reference;
    }
}
