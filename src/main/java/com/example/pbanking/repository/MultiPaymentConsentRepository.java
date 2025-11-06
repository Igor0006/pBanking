package com.example.pbanking.repository;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.pbanking.model.MultiPaymentConsent;
import com.example.pbanking.model.User;

import jakarta.transaction.Transactional;

public interface MultiPaymentConsentRepository extends JpaRepository<MultiPaymentConsent, Long> {
    @Query("""
            select c from MultiPaymentConsent c where c.user = :user and
            c.debtorAccount = :debtorAccount and c.maxUses > 0 and c.maxAmountPerPayment >= :amount
            and c.maxTotalAmount >= :amount order by c.id limit 1
            """)
    Optional<MultiPaymentConsent> findAppropriateConsent(@Param("user") User user,
            @Param("debtorAccount") String debtorAccount, @Param("amount") BigDecimal amount);

    @Modifying
    @Transactional
    @Query("""
            update MultiPaymentConsent c set c.maxUses = c.maxUses - 1,
            c.maxTotalAmount = c.maxTotalAmount - :amount where c.user = :user and
            c.debtorAccount = :debtorAccount and c.maxUses > 0 and
            c.maxAmountPerPayment >= :amount and c.maxTotalAmount >= :amount
            """)
    void markUsage(@Param("user") User user,
    @Param("debtorAccount") String debtorAccount, @Param("amount") BigDecimal amount);
}
