package com.example.pbanking.bank;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.pbanking.bank.BankEntity;

@Repository
public interface BankRepository extends JpaRepository<BankEntity, String> {
    
}
