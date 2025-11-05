package com.example.pbanking.service;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.pbanking.config.TPPConfig;
import com.example.pbanking.dto.response.TransactionsResponse;
import com.example.pbanking.model.enums.ConsentType;
import com.example.pbanking.model.enums.PurposeType;
import com.example.pbanking.repository.TransactionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private final static String TRANSACTIONS_PATH = "/transactions";
    private final static String ACCOUNTS_PATH = "/accounts";
    
    private final TPPConfig props;
    private final ConsentService consentService;
    private final TransactionRepository transactionRepository;
    private final BankTokenService tokenService;
    private final WebClientExecutor wc;

    
    public TransactionsResponse getTransactions(String bankId, String accountId, Map<String, String> queryMap) {
        var headersMap = Map.of("x-consent-id", consentService.getConsentForBank(bankId, ConsentType.READ),
                "x-requesting-bank", props.getRequestingBankId());
        String path = ACCOUNTS_PATH + "/" + accountId + TRANSACTIONS_PATH;
        return wc.get(bankId, path, queryMap, headersMap, tokenService.getBankToken(bankId),
                TransactionsResponse.class);
    }
    
    public TransactionsResponse getTransactionsPrime(String bank_id, String account_id, Map<String, String> queryMap) {
        var response = getTransactions(bank_id, account_id, queryMap);
        for (var transaction : response.transactions()) {
            transactionRepository.findTypeByTransactionId(transaction.getTransactionId())
                .ifPresent(transaction::setType);
        }
        return response;
    }

    public void setTypeForTransaction(String transaction_id, PurposeType type) {
        var transaction = transactionRepository.findByTransactionId(transaction_id)
                .orElseGet(() -> {
                    var newTransaction = new com.example.pbanking.model.Transaction();
                    newTransaction.setTransactionId(transaction_id);
                    
                    return newTransaction;
                });

        transaction.setType(type);
        transactionRepository.save(transaction);
    }
}
