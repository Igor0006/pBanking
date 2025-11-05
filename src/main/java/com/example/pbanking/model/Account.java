package com.example.pbanking.model;

import com.example.pbanking.model.enums.PurposeType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "accounts")
public class Account {
    @Id
    @Column(name = "account_id")
    private String accountId;
    
    @Enumerated(EnumType.STRING)
    private PurposeType type;
    
    private String description;
}
