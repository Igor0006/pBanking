package com.example.pbanking.transaction;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.pbanking.account.AccountRepository;
import com.example.pbanking.bank.BankTokenService;
import com.example.pbanking.common.client.WebClientExecutor;
import com.example.pbanking.common.enums.ConsentType;
import com.example.pbanking.common.enums.PurposeType;
import com.example.pbanking.config.TPPConfig;
import com.example.pbanking.consent.ConsentService;
import com.example.pbanking.transaction.dto.response.TransactionsResponse;

import com.example.pbanking.exception.BadRequestException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private final static String TRANSACTIONS_PATH = "/transactions";
    private final static String ACCOUNTS_PATH = "/accounts";
    
    private final TPPConfig props;
    private final ConsentService consentService;
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final BankTokenService tokenService;
    private final WebClientExecutor wc;
    private final ClassifierService classifierService;
        
    public TransactionsResponse getTransactions(String bankId, String accountId, Map<String, String> queryMap) {
        var headersMap = Map.of("x-consent-id", consentService.getConsentForBank(bankId, ConsentType.READ, null),
                "x-requesting-bank", props.getRequestingBankId());
        String path = ACCOUNTS_PATH + "/" + accountId + TRANSACTIONS_PATH;
        var response =  wc.get(bankId, path, queryMap, headersMap, tokenService.getBankToken(bankId),
                TransactionsResponse.class);
        if (response != null) {
            response.transactions().forEach(transaction -> transaction.setBankId(bankId));
        }
        return response;
    }
    
    public TransactionsResponse getTransactionsPrime(String bank_id, String account_id, Map<String, String> queryMap, boolean predict) {
        var response = getTransactions(bank_id, account_id, queryMap);
        for (var transaction : response.transactions()) {
            if (transaction.getType() != null && transaction.getType() != PurposeType.NONE) {
                continue;
            }

            boolean typeAssigned = transactionRepository.findTypeByTransactionId(transaction.getTransactionId())
                    .map(type -> {
                        transaction.setType(type);
                        return true;
                    })
                    .orElse(false);

            if (typeAssigned) {
                continue;
            }

            var transactionAccountId = transaction.getAccountId();
            if (transactionAccountId != null && !transactionAccountId.isBlank()) {
                var account = accountRepository.findByAccountIdAndBankId(transactionAccountId, bank_id);
                if (account.isPresent() && account.get().getType() != null) {
                    transaction.setType(account.get().getType());
                }
            }
            if (transaction.getType() != null && transaction.getType() != PurposeType.NONE) {
                continue;
            }
            if (predict) {
                var type = classifierService.predictType(transaction);
                setTypeForTransaction(transaction.getTransactionId(), type);
                transaction.setType(type);
            }
        }
        return response;
    }
    
    public void setTypeForTransaction(String transactionId, PurposeType type) {
        if (transactionId == null || transactionId.isBlank()) {
            throw new BadRequestException("Transaction id must not be empty");
        }

        var transaction = transactionRepository.findByTransactionId(transactionId)
                .orElseGet(() -> {
                    var newTransaction = new Transaction();
                    newTransaction.setTransactionId(transactionId);
                    
                    return newTransaction;
                });

        transaction.setType(type);
        transaction.setTransactionId(transactionId);
        transactionRepository.save(transaction);
    }
}
