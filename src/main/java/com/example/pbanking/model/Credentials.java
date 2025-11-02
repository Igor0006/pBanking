package com.example.pbanking.model;

import java.time.Instant;

import com.example.pbanking.model.enums.ConsentStatus;
import com.example.pbanking.model.enums.ConsentType;
import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;

@Data
@Entity
@Table(
  name = "credentials",
  uniqueConstraints = @UniqueConstraint(columnNames = {"bank_id", "user_id", "client_id"})
)
public class Credentials {
    @Id
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
    private ConsentType type;

    @Enumerated(EnumType.STRING)
    private ConsentStatus status;
    
    private Instant expirationDate;

    private String clientId;
}
