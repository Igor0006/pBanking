package com.example.pbanking.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public abstract class BasePaymentRequestBody {
    protected String requesting_bank;
    
    protected String client_id;
    protected String consent_type;
    protected String debtor_account;
}
