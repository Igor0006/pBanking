package com.example.pbanking.consent;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.pbanking.consent.Credentials;
import com.example.pbanking.user.User;

@Repository
public interface CredentialsRepository extends JpaRepository<Credentials, Long> {

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

    @Query(value = """
            select c.client_id from credentials c
            where c.user_id = :userId and c.bank_id = :bankId limit 1
            """, nativeQuery = true)
    Optional<String> findClientIdByUserAndBank(@Param("userId") UUID userId, @Param("bankId") String bankId);
}
