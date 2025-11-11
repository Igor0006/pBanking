package com.example.pbanking.transaction;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.pbanking.transaction.Transaction;
import com.example.pbanking.common.enums.PurposeType;

public interface TransactionRepository extends JpaRepository<Transaction, String>  {
    Optional<Transaction> findByTransactionId(String transactionId);

    @Query("SELECT t.type FROM Transaction t WHERE t.transactionId = :id")
    Optional<PurposeType> findTypeByTransactionId(@Param("id") String id);

}
