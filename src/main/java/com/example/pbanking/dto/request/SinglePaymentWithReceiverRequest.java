package com.example.pbanking.dto.request;

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
public class SinglePaymentWithReceiverRequest extends BasePaymentRequestBody{

    private String creditor_account;
    private String creditor_name;
    private BigDecimal amount;
    private String currency;
    private String reference;

    public SinglePaymentWithReceiverRequest(String requesting_bank, String client_id, String consent_type,
            BigDecimal amount, String currency, String debtor_account, String creditor_account, String creditor_name, String reference) {
        super(requesting_bank, client_id, consent_type, debtor_account);
            this.creditor_account = creditor_account;
            this.creditor_name = creditor_name;
            this.amount = amount;
            this.reference = reference;
            this.currency = currency;
    }    
}
