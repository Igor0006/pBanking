package com.example.pbanking.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.pbanking.config.BanksProperties;
import com.example.pbanking.dto.BankEntry;
import com.example.pbanking.exception.ConflictException;
import com.example.pbanking.exception.InternalServerException;
import com.example.pbanking.exception.NotFoundException;
import com.example.pbanking.model.BankEntity;
import com.example.pbanking.repository.BankRepository;

import jakarta.transaction.Transactional;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class BankService {
    private final BankRepository bankRepository;
    private final BanksProperties banksProperties;
    private final EncryptionService encryptionService;

    @PostConstruct
    void seedBanksFromConfig() {
        List<BankEntry> configuredBanks = banksProperties.getList();
        if (configuredBanks == null || configuredBanks.isEmpty()) {
            log.warn("No banks defined in banks.yml; skipping seeding.");
            return;
        }
        configuredBanks.forEach(entry -> bankRepository.findById(entry.id())
                .map(existing -> {
                    boolean updated = false;
                    if (entry.url() != null && !entry.url().equals(existing.getUrl())) {
                        existing.setUrl(entry.url());
                        updated = true;
                    }
                    if (updated) {
                        log.info("Updating bank {} details from configuration", entry.id());
                        bankRepository.save(existing);
                    }
                    return existing;
                })
                .orElseGet(() -> {
                    log.info("Seeding bank {} from configuration", entry.id());
                    return bankRepository.save(new BankEntity(entry.id(), entry.name(), entry.url()));
                }));
    }

    public BankEntity getBankFromId(String bankId) {
        Optional<BankEntity> optionalBank = bankRepository.findById(bankId);
        if (!optionalBank.isPresent()) {
            throw new NotFoundException("Bank not found");
        }
        return optionalBank.get();
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


    public List<BankEntry> getAvailableBanks() {
        return bankRepository.findAll().stream()
                .map(bank -> new BankEntry(bank.getBankId(), bank.getName(), bank.getUrl()))
                .toList();
    }

    @Transactional
    public BankEntity createBank(String bankId, String name, String url) {
        if (bankRepository.existsById(bankId)) {
            throw new ConflictException("Bank with id " + bankId + " already exists");
        }
        BankEntity bank = new BankEntity(bankId, name, url);
        return bankRepository.save(bank);
    }

    public record StoredToken(String value, Instant expiresAt) {}
}
