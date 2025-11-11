package com.example.pbanking.consent;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "multi_payment_consents")
@Getter
@Setter
@NoArgsConstructor
@PrimaryKeyJoinColumn(name = "consent_id")
@DiscriminatorValue("PAYMENT_Multi")
public class MultiPaymentConsent extends Credentials {
    @Column(columnDefinition = "TEXT", name = "debtor_account")
    private String debtorAccount;
    
    @Column(name = "max_uses")
    private int maxUses;

    @Column(name = "max_amount_per_payment")
    private BigDecimal maxAmountPerPayment;

    @Column(name = "max_total_amount")
    private BigDecimal maxTotalAmount;


}
