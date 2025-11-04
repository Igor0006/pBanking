package com.example.pbanking.model;

import java.time.Instant;

import com.example.pbanking.model.enums.ConsentStatus;
import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(
  name = "credentials"
)
@DiscriminatorColumn(name = "consent_type", discriminatorType = DiscriminatorType.STRING)
public class Credentials {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(columnDefinition = "TEXT")
    private String consent;

    @ManyToOne
    @JoinColumn(name = "bank_id")
    @JsonBackReference
    private BankEntity bank;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonBackReference
    private User user;

    @Enumerated(EnumType.STRING)
    private ConsentStatus status;
    
    @Column(name = "expiration_date")
    private Instant expirationDate;

    private String clientId;
}
