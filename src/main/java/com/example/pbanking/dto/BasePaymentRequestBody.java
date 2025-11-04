package com.example.pbanking.dto;

import org.springframework.beans.factory.annotation.Value;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public abstract class BasePaymentRequestBody {
    @Value("${bank.id}")
    protected String requesting_bank;
    
    protected String client_id;
    protected String consent_type;
    protected String debtor_account;
}
