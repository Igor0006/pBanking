package com.example.pbanking.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.pbanking.model.AccountConsent;
import com.example.pbanking.model.BankEntity;
import com.example.pbanking.model.User;

public interface AccountConsentRepository extends JpaRepository<AccountConsent, Long> {
    @Query("""
            select c from AccountConsent c where c.user = :user 
            and c.bank = :bank and c.expirationDate > CURRENT_TIMESTAMP
            """)
    Optional<AccountConsent> findByUserAndBank(User user, BankEntity bank);
}
