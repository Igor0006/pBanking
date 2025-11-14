package com.example.pbanking.transaction;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.example.pbanking.account.AccountRepository;
import com.example.pbanking.bank.BankTokenService;
import com.example.pbanking.common.client.WebClientExecutor;
import com.example.pbanking.common.enums.ConsentType;
import com.example.pbanking.common.enums.PurposeType;
import com.example.pbanking.config.TPPConfig;
import com.example.pbanking.consent.ConsentService;
import com.example.pbanking.transaction.dto.response.TransactionsResponse;
import com.example.pbanking.transaction.dto.response.TransactionsSummaryResponse.TransactionDto;
import com.example.pbanking.user.UserService;
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
    private final KafkaTemplate<String, TransactionDto> kafkaTemplate;
    private final UserService userService;
    private final ClassifierApiService classifierApiService;

    @Value("${kafka.topics.manual:transactions.manual}")
    private String manualTopic;
        
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
        String currentUserId = null;
        if (predict) {
            var currentUser = userService.getCurrentUser();
            if (currentUser != null && currentUser.getId() != null) {
                currentUserId = currentUser.getId().toString();
            }
        }
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
            if (!predict || currentUserId == null) {
                continue;
            }

            var dto = TransactionDto.from(transaction);
            if (dto == null) {
                continue;
            }

            var predictedType = classifierApiService.predict(dto, currentUserId);
            if (predictedType == null || predictedType == PurposeType.NONE) {
                continue;
            }

            transaction.setType(predictedType);
            dto.setType(predictedType);
            setTypeForTransaction(dto, predictedType);
        }
        return response;
    }
    
    public void setTypeForTransaction(TransactionDto transaction, PurposeType type) {
        String transactionId = transaction.getTransactionId();
        if (transactionId == null || transactionId.isBlank()) {
            throw new BadRequestException("Transaction id must not be empty");
        }

        var tr = transactionRepository.findByTransactionId(transactionId)
                .orElseGet(() -> {
                    var newTransaction = new Transaction();
                    newTransaction.setTransactionId(transactionId);
                    
                    return newTransaction;
                });

        tr.setType(type);
        tr.setTransactionId(transactionId);
        transactionRepository.save(tr);
        
        transaction.setType(type);
        sendToClassifierService(transaction);
    }
    
    private void sendToClassifierService(TransactionDto tr) {
        kafkaTemplate.send(manualTopic, userService.getCurrentUser().getId().toString(), tr);
    }
}
