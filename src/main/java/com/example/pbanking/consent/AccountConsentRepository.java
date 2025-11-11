package com.example.pbanking.consent;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.pbanking.consent.AccountConsent;
import com.example.pbanking.bank.BankEntity;
import com.example.pbanking.user.User;

public interface AccountConsentRepository extends JpaRepository<AccountConsent, Long> {
    @Query("""
            select c from AccountConsent c where c.user = :user 
            and c.bank = :bank and c.expirationDate > CURRENT_TIMESTAMP
            """)
    Optional<AccountConsent> findByUserAndBank(User user, BankEntity bank);
}
