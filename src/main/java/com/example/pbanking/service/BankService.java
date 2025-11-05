package com.example.pbanking.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.pbanking.config.BanksProperties;
import com.example.pbanking.dto.BankEntry;
import com.example.pbanking.exception.InternalServerException;
import com.example.pbanking.exception.NotFoundException;
import com.example.pbanking.model.BankEntity;
import com.example.pbanking.repository.BankRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class BankService {
    private final BankRepository bankRepository;
    private final BanksProperties banksProperties;
    private final EncryptionService encryptionService;

    public BankEntity getBankFromId(String bankId) {
        Optional<BankEntity> optionalBank = bankRepository.findById(bankId);
        if (!optionalBank.isPresent()) {
            return addBank(bankId);
        }
        return optionalBank.get();
    }

    /**
     * Add a bank to the database if it presents in bank.yml
     * 
     * @return BankEntity
     */
    private BankEntity addBank(String bankId) {
        List<BankEntry> banks = banksProperties.getList();

        for (BankEntry bankEntry : banks) {
            if (bankEntry.id().equalsIgnoreCase(bankId)) {
                BankEntity bank = new BankEntity(bankEntry.id(), bankEntry.name());
                return bankRepository.save(bank);
            }
        }

        throw new NotFoundException("Bank not found: " + bankId);
    }
    
    @Transactional
    public void saveToken(String bankId, String token, Instant expiresAt) {
        if (token == null) {
            throw new InternalServerException("Received token for bank " + bankId + " is null");
        }
        BankEntity bank = getBankFromId(bankId);
        bank.setToken(encryptionService.encrypt(token));
        bank.setExpiresAt(expiresAt);
        bankRepository.save(bank);
    }

    public Optional<StoredToken> getStoredToken(String bankId) {
        return bankRepository.findById(bankId)
                .flatMap(bank -> {
                    if (bank.getToken() == null || bank.getExpiresAt() == null) {
                        return Optional.empty();
                    }
                    try {
                        String decrypted = encryptionService.decrypt(bank.getToken());
                        return Optional.of(new StoredToken(decrypted, bank.getExpiresAt()));
                    } catch (RuntimeException ex) {
                        log.warn("Stored token for bank {} is invalid, clearing it and requesting a fresh one.", bankId, ex);
                        bank.setToken(null);
                        bank.setExpiresAt(null);
                        bankRepository.save(bank);
                        return Optional.empty();
                    }
                });
    }

    public record StoredToken(String value, Instant expiresAt) {}
}
