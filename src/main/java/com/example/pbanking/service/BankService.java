package com.example.pbanking.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.pbanking.config.BanksProperties;
import com.example.pbanking.dto.BankEntry;
import com.example.pbanking.model.BankEntity;
import com.example.pbanking.repository.BankRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BankService {
    private final BankRepository bankRepository;
    private final BanksProperties banksProperties;
    private final EncryptionService encryptionService;

    /**
     * Gets a bank entity by bankId. If it is not present in the database, but
     * is present in the banks.yml file, a new entry is created beforehand.
     * 
     * @return BankEntity
     */
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

        throw new IllegalArgumentException("No such bank: " + bankId);
    }

    public void saveToken(String bankId, String token, Instant expiresAt) {
        if (token == null) {
            throw new IllegalArgumentException("Recieved token for bank " + bankId + " is null");
        }
        BankEntity bank = getBankFromId(bankId);
        bank.setToken(encryptionService.encrypt(token));
        bank.setExpiresAt(expiresAt);
    }

    public Optional<StoredToken> getStoredToken(String bankId) {
        return bankRepository.findById(bankId)
                .flatMap(bank -> {
                    if (bank.getToken() == null || bank.getExpiresAt() == null) {
                        return Optional.empty();
                    }
                    String decrypted = encryptionService.decrypt(bank.getToken());
                    return Optional.of(new StoredToken(decrypted, bank.getExpiresAt()));
                });
    }

    public record StoredToken(String value, Instant expiresAt) {}
}
