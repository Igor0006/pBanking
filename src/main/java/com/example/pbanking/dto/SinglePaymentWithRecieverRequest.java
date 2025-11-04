package com.example.pbanking.dto;

import java.math.BigDecimal;

import lombok.Getter;

// public record SinglePaymentWithRecieverRequest(
//         String requesting_bank,
//         String client_id,
//         String consent_type,
//         double amount,
//         String debtor_account,
//         String creditor_account,
//         String reference) {

// }

@Getter
public class SinglePaymentWithRecieverRequest extends SinglePaymentWithoutRecieverRequest{

    private String creditor_account;
    private String creditor_name;

    public SinglePaymentWithRecieverRequest(String requesting_bank, String client_id, String consent_type,
            BigDecimal amount, String debtor_account, String creditor_account, String creditor_name, String reference) {
        super(requesting_bank, client_id, consent_type, amount, debtor_account, reference);
            this.creditor_account = creditor_account;
            this.creditor_name = creditor_name;
    }    
}
