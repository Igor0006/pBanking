package com.example.pbanking.consent;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.pbanking.bank.BankEntity;
import com.example.pbanking.user.User;

import jakarta.transaction.Transactional;

@Repository
public interface SinglePaymentConsentRepository extends JpaRepository<SinglePaymentConsent, Long> {
    @Query("SELECT c from SinglePaymentConsent c WHERE c.user = :user AND c.creditorAccount = :creditorAccount AND c.isUsed = false")
    Optional<SinglePaymentConsent> findByUserAndCreditorAccount(@Param("user") User user,
            @Param("creditorAccount") String creditorAccount);

    Optional<SinglePaymentConsent> findByDebtorAccountAndCreditorAccount(@Param("debtorAccount") String debtorAccount,
            @Param("creditorAccount") String creditorAccount);

    @Query("""
            select c from SinglePaymentConsent c where
            c.user = :user and c.debtorAccount = :debtorAccount and
            c.creditorAccount = :creditorAccount and c.amount >= :amount and c.isUsed = false and c.expirationDate > :currentTime
            """)
    Optional<SinglePaymentConsent> findAppropriateConsent(@Param("user") User user,
            @Param("debtorAccount") String debtorAccount,
            @Param("creditorAccount") String creditorAccount, @Param("amount") BigDecimal amount, @Param("currentTime") Instant currentTime);

    Optional<SinglePaymentConsent> findByUserAndBank(User user, BankEntity bank);

    @Query("""
        update SinglePaymentConsent c set c.isUsed = true where
        c.user = :user and c.debtorAccount = :debtorAccount and
        c.creditorAccount = :creditorAccount and c.amount >= :amount and c.isUsed = false
        """)
    @Modifying
    @Transactional
    void markAsUsed(@Param("user") User user,
    @Param("debtorAccount") String debtorAccount,
    @Param("creditorAccount") String creditorAccount, @Param("amount") BigDecimal amount);
}
