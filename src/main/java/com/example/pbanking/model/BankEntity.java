package com.example.pbanking.model;

import java.time.Instant;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "banks")
public class BankEntity {
    @Id
    private String bankId;
    private String name;

    @Column(columnDefinition = "TEXT")
    private String token;
    private Instant expiresAt;

    @OneToMany(mappedBy = "bank")
    private List<Credentials> consents;

    public BankEntity(String bankId, String bankName) {
        this.bankId = bankId;
        this.name = bankName;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }
}
