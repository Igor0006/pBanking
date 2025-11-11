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
@Table(name = "single_payment_consents")
@Getter
@Setter
@NoArgsConstructor
@PrimaryKeyJoinColumn(name = "consent_id")
@DiscriminatorValue("PAYMENT_SINGLE")
public class SinglePaymentConsent extends Credentials {

    @Column(columnDefinition = "TEXT", name = "debtor_account")
    private String debtorAccount;
 
    @Column(columnDefinition = "TEXT", name = "creditor_account")
    private String creditorAccount;

    @Column(name = "is_used")
    private boolean isUsed;
    private BigDecimal amount;
}
