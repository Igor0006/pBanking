package com.example.pbanking.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.pbanking.model.BankEntity;
import com.example.pbanking.model.Credentials;
import com.example.pbanking.model.User;
import com.example.pbanking.model.enums.ConsentType;

public interface CredentialsRepository extends JpaRepository<Credentials, String> {

    Optional<Credentials> findByUserAndBankAndType(User user, BankEntity bank, ConsentType type);

    Optional<Credentials> findByUserAndBankAndClientId(User user, BankEntity bank, String clientId);

    Optional<Credentials> findByUserAndBankAndTypeAndClientId(User user, BankEntity bank, ConsentType type, String clientId);

    interface BankClientPair {
        String getBankId();
        String getClientId();
    }

    @Query("""
            select distinct c.bank.bankId as bankId, c.clientId as clientId
            from Credentials c
            where c.user = :user and c.clientId is not null
            order by c.bank.bankId, c.clientId
            """)
    List<BankClientPair> findBankClientPairsByUser(@Param("user") User user);

    @Query("""
            select distinct c.bank.bankId as bankId, c.clientId as clientId
            from Credentials c
            where c.clientId is not null
            order by c.bank.bankId, c.clientId
            """)
    List<BankClientPair> findAllBankClientPairs();
}
