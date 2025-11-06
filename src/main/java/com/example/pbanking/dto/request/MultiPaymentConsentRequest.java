package com.example.pbanking.dto.request;

import java.math.BigDecimal;

import lombok.Getter;

@Getter
public class MultiPaymentConsentRequest extends BasePaymentConsentRequestBody {
    private int max_uses;
    private BigDecimal max_amount_per_payment;
    private BigDecimal max_total_amount;
    private String valid_until;

    public MultiPaymentConsentRequest(String requesting_bank, String client_id, String consent_type,
            String debtor_account, int max_uses, BigDecimal max_amount_per_payment, BigDecimal max_total_amount,
            String valid_until) {
        super(requesting_bank, client_id, consent_type, debtor_account);
        this.max_uses = max_uses;
        this.max_amount_per_payment = max_amount_per_payment;
        this.max_total_amount = max_total_amount;
        this.valid_until = valid_until;
    }

}
