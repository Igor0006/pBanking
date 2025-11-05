package com.example.pbanking.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.pbanking.model.AccountConsent;
import com.example.pbanking.model.BankEntity;
import com.example.pbanking.model.User;

public interface AccountConsentRepository extends JpaRepository<AccountConsent, Long> {
    Optional<AccountConsent> findByUserAndBank(User user, BankEntity bank);
}
