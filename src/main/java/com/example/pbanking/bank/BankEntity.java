package com.example.pbanking.bank;

import java.time.Instant;
import java.util.List;

import com.example.pbanking.consent.Credentials;
import com.fasterxml.jackson.annotation.JsonManagedReference;

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
    private String url;

    @Column(columnDefinition = "TEXT")
    private String token;
    private Instant expiresAt;

    @OneToMany(mappedBy = "bank")
    @JsonManagedReference
    private List<Credentials> consents;

    public BankEntity(String bankId, String bankName, String bankUrl) {
        this.bankId = bankId;
        this.name = bankName;
        this.url = bankUrl;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
