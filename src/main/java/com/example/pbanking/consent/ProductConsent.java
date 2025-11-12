package com.example.pbanking.consent;

import java.math.BigDecimal;
import java.util.List;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "product_consents")
@NoArgsConstructor
@PrimaryKeyJoinColumn(name = "consent_id")
@DiscriminatorValue("PRODUCT")
@Getter
@Setter
public class ProductConsent extends Credentials{
    private boolean readProductAgreements;
    private boolean openProductAgreements;
    private boolean closeProductAgreements;
    private List<String> allowedProductTypes;
    private BigDecimal maxAmount;
}
