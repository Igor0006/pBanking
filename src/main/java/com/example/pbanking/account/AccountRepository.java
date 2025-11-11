package com.example.pbanking.account;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;



public interface AccountRepository extends JpaRepository<Account, Account.AccountKey> {
    Optional<Account> findByAccountIdAndBankId(String accountId, String bankId);

    
} 
