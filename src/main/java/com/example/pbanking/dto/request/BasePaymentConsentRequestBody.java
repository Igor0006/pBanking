package com.example.pbanking.dto.request;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "consent_type"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = SinglePaymentWithReceiverRequest.class, name = "single_use")

})
public abstract class BasePaymentConsentRequestBody {
    protected String requesting_bank;
    
    protected String client_id;
    protected String consent_type;
    protected String debtor_account;
}
