package com.example.pbanking.service;

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

    /**
     * Gets a bank entity by bankId. If it is not present in the database, but
     * is present in the banks.yml file, a new entry is created beforehand.
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
}
