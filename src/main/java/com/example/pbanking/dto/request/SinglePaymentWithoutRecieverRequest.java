package com.example.pbanking.dto.request;

import java.math.BigDecimal;

import lombok.Getter;

// public record SinglePaymentWithoutRecieverRequest(
//         String requesting_bank,
//         String client_id,
//         String consent_type,
//         double amount,
//         String reference) {

// }

@Getter
public class SinglePaymentWithoutRecieverRequest extends BasePaymentRequestBody {
    protected BigDecimal amount;
    protected String reference;

    public SinglePaymentWithoutRecieverRequest(String requesting_bank, String client_id, String consent_type,
            BigDecimal amount, String debtor_account, String reference) {
        super(requesting_bank, client_id, consent_type, debtor_account);
        this.amount = amount;
        this.reference = reference;
    }

}
