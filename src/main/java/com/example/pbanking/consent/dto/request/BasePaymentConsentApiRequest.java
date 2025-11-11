package com.example.pbanking.consent.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public abstract class BasePaymentConsentApiRequest {
    protected String bank_id;
    protected String debtor_account;
}
