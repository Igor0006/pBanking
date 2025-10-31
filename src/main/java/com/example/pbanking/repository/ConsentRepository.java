package com.example.pbanking.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.pbanking.model.Consent;
import com.example.pbanking.model.User;
import com.example.pbanking.model.enums.Bank;
import com.example.pbanking.model.enums.ConsentType;

public interface ConsentRepository extends JpaRepository<Consent, String> {
    Optional<Consent> findByUserAndBankAndType(User user, Bank bank, ConsentType type);
}
