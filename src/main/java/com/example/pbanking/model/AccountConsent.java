package com.example.pbanking.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;

@Entity
@DiscriminatorValue("READ")
@PrimaryKeyJoinColumn(name = "consent_id")
public class AccountConsent extends Credentials {
    
}
