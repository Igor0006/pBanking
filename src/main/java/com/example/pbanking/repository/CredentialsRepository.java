package com.example.pbanking.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.pbanking.model.BankEntity;
import com.example.pbanking.model.Credentials;
import com.example.pbanking.model.User;
import com.example.pbanking.model.enums.ConsentType;

public interface CredentialsRepository extends JpaRepository<Credentials, String> {
    Optional<Credentials> findByUserAndBankAndType(User user, BankEntity bank, ConsentType type);

    Optional<Credentials> findByUserAndBankAndClientId(User user, BankEntity bank, String clientId);
}
