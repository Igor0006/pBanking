package com.example.pbanking.model;

import java.time.Instant;

import com.example.pbanking.model.enums.ConsentType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "credentials")
public class Credentials {
    @Id
    @Column(columnDefinition = "TEXT")
    private String consent;

    @ManyToOne
    @JoinColumn(name = "bank_id")
    private BankEntity bank;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    private ConsentType type;

    private String status;
    private Instant expirationDate;

    private String clientId;
}
