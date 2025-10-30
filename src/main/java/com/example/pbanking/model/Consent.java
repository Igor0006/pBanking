package com.example.pbanking.model;

import com.example.pbanking.dto.enums.Bank;

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
@Table(name = "consents")
public class Consent {
    @Id
    @Column(columnDefinition = "TEXT")
    private String consent;

    @Enumerated(EnumType.STRING)
    private Bank bank;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
