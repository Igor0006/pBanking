package com.example.pbanking.model;

import java.io.Serializable;

import com.example.pbanking.model.enums.PurposeType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "accounts")
@IdClass(Account.AccountKey.class)
public class Account {
    @Id
    @Column(name = "account_id")
    private String accountId;

    @Id
    @Column(name = "bank_id")
    private String bankId;
    
    @Enumerated(EnumType.STRING)
    private PurposeType type;
    
    private String description;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AccountKey implements Serializable {
        private static final long serialVersionUID = 1L;

        private String accountId;
        private String bankId;
    }
}
