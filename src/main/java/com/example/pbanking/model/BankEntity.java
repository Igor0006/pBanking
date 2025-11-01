package com.example.pbanking.model;

import java.util.List;

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
    private String token;

    @OneToMany(mappedBy = "bank")
    private List<Consent> consents;

    public BankEntity(String bankId, String bankName) {
        this.bankId = bankId;
        this.name = bankName;
    }

    // public void setBankId(String bankId) {
    //     this.bankId = bankId;
    // }

    // public void setName(String bankName) {
    //     this.name = bankName;
    // }
}
