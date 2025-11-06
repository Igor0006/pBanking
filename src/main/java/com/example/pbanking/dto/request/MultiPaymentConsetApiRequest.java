package com.example.pbanking.dto.request;

import java.math.BigDecimal;

import lombok.Getter;

@Getter
public class MultiPaymentConsetApiRequest extends BasePaymentConsentApiRequest {
    
    private int max_uses;
    private BigDecimal max_amount_per_payment;
    private BigDecimal max_total_amount;
    private String valid_until;

    public MultiPaymentConsetApiRequest(String bank_id, String debtor_account, int max_uses,
            BigDecimal max_amount_per_payment, BigDecimal max_total_amount, String valid_until) {
        super(bank_id, debtor_account);
        this.max_uses = max_uses;
        this.max_amount_per_payment = max_amount_per_payment;
        this.max_total_amount = max_total_amount;
        this.valid_until = valid_until;
    }

    
}
