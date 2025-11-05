package com.example.pbanking.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.pbanking.config.TPPConfig;
import com.example.pbanking.dto.AccountSummary;
import com.example.pbanking.dto.response.AccountsResponse;
import com.example.pbanking.dto.response.BalanceResponse;
import com.example.pbanking.model.enums.ConsentType;
import com.example.pbanking.model.enums.PurposeType;
import com.example.pbanking.repository.AccountRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final WebClientExecutor wc;
    private final BankTokenService tokenService;
    private final ConsentService consentService;
    private final AccountRepository accountRepository;
    private final TPPConfig props;

    private static final String ACCOUNTS_PATH = "/accounts";

    public List<AccountSummary> getAccounts(String bankId, String clientId) {
        var headersMap = Map.of(
                "x-requesting-bank", props.getRequestingBankId(),
                "x-consent-id", consentService.getConsentForBank(bankId, ConsentType.READ));
        var queryMap = Map.of("client_id", clientId);
        var response = wc.get(bankId, ACCOUNTS_PATH, queryMap, headersMap,
                tokenService.getBankToken(bankId), AccountsResponse.class);
        return response.accounts().stream()
                .map(acc -> new AccountSummary(
                        acc.accountId(), bankId, acc.status(), acc.currency(), acc.accountSubType(),
                        acc.nickname(), acc.openingDate(), acc.accountReferences(),
                        getAccountBalance(bankId, acc.accountId())))
                .toList();
    }
    
    public List<AccountSummary> getAccountsPrime(String bankId, String clientId) {
        var accounts = getAccounts(bankId, clientId);
        for (var account : accounts) {
            accountRepository.findByAccountIdAndBankId(account.getAccountId(), bankId).ifPresent(accEntity -> {
                account.setPurposeType(accEntity.getType());
                account.setDescription(accEntity.getDescription());
            });
        }
        return accounts;
    } 
    
    public void setTypeForAccount(String bank_id, String account_id, PurposeType type) {
        var account = accountRepository.findByAccountIdAndBankId(account_id, bank_id)
                .orElseGet(() -> {
                    var newAccount = new com.example.pbanking.model.Account();
                    newAccount.setAccountId(account_id);
                    newAccount.setBankId(bank_id);
                    return newAccount;
                });
        account.setType(type);
        accountRepository.save(account);
    }
    
    public void setDescription(String bank_id, String account_id, String description) {
        var account = accountRepository.findByAccountIdAndBankId(account_id, bank_id)
                .orElseGet(() -> {
                    var newAccount = new com.example.pbanking.model.Account();
                    newAccount.setAccountId(account_id);
                    newAccount.setBankId(bank_id);
                    return newAccount;
                });
        account.setDescription(description);
        accountRepository.save(account);
    }
    

    public BigDecimal getAccountBalance(String bankId, String accountId) {
        var headersMap = Map.of(
                "x-requesting-bank", props.getRequestingBankId(),
                "x-consent-id", consentService.getConsentForBank(bankId, ConsentType.READ));
        String path = ACCOUNTS_PATH + "/" + accountId + "/balances";
        var response = wc.get(bankId, path, null, headersMap,
                tokenService.getBankToken(bankId), BalanceResponse.class);
        if (response == null || response.data() == null ||
                response.data().balance() == null || response.data().balance().isEmpty()) {
            return BigDecimal.ZERO;
        }
        var balance = response.data().balance().get(0);
        if (balance.amount() == null || balance.amount().amount() == null)
            return BigDecimal.ZERO;
        return balance.amount().amount();
    }
}
