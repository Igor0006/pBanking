package com.example.pbanking.consent;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.pbanking.user.User;

import jakarta.transaction.Transactional;

public interface MultiPaymentConsentRepository extends JpaRepository<MultiPaymentConsent, Long> {
    @Query("""
            select c from MultiPaymentConsent c where c.user = :user and
            c.debtorAccount = :debtorAccount and c.maxUses > 0 and c.maxAmountPerPayment >= :amount
            and c.maxTotalAmount >= :amount and c.expirationDate > :currentTime order by c.id limit 1
            """)
    Optional<MultiPaymentConsent> findAppropriateConsent(@Param("user") User user,
            @Param("debtorAccount") String debtorAccount, @Param("amount") BigDecimal amount, @Param("currentTime") Instant currentTime);

    @Modifying
    @Transactional
    @Query(value = """
        UPDATE multi_payment_consents
        SET max_uses = max_uses - 1,
            max_total_amount = max_total_amount - :amount 
        WHERE consent_id = (
            SELECT consent_id FROM multi_payment_consents 
            WHERE debtor_account = :debtorAccount 
            AND max_uses > 0 
            AND max_amount_per_payment >= :amount 
            AND max_total_amount >= :amount 
            ORDER BY consent_id LIMIT 1)
            """, nativeQuery = true)
    void markUsage(
    @Param("debtorAccount") String debtorAccount, @Param("amount") BigDecimal amount);
}
