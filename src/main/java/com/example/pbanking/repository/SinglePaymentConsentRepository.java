package com.example.pbanking.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.pbanking.model.BankEntity;
import com.example.pbanking.model.SinglePaymentConsent;
import com.example.pbanking.model.User;

@Repository
public interface SinglePaymentConsentRepository extends JpaRepository<SinglePaymentConsent, Long> {
    @Query("SELECT c from SinglePaymentConsent c WHERE c.user = :user AND c.creditorAccount = :creditorAccount AND c.isUsed = false")
    Optional<SinglePaymentConsent> findByUserAndCreditorAccount(@Param("user") User user,
            @Param("creditorAccount") String creditorAccount);

    Optional<SinglePaymentConsent> findByDebtorAccountAndCreditorAccount(@Param("debtorAccount") String debtorAccount,
            @Param("creditorAccount") String creditorAccount);

    Optional<SinglePaymentConsent> findByUserAndBank(User user, BankEntity bank);
}
