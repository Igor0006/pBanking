package com.example.pbanking.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.pbanking.config.TPPConfig;
import com.example.pbanking.dto.AccountsResponse;
import com.example.pbanking.dto.AvailableProductsResponse;
import com.example.pbanking.dto.TransactionsResponse;
import com.example.pbanking.dto.AccountsResponse.Account;
import com.example.pbanking.dto.AvailableProductsResponse.Product;
import com.example.pbanking.model.enums.ConsentType;
import com.example.pbanking.repository.CredentialsRepository.BankClientPair;
import com.example.pbanking.dto.TransactionsResponse.TransactionStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DataRecieveService {
    private final WebClientExecutor wc;
    private final BankTokenService tokenService;
    private final ConsentService consentService;
    private final UserService userService;
    private final TPPConfig props;
    private final static String ACCOUNTS_PATH = "/accounts";
    private final static String TRANSACTIONS_PATH = "/transactions";
    private final static String AVAILABLE_PRODUCTS_PATH = "/products";
    
    public List<Account> getAccounts(String bank_id, String client_id) {
        var headersMap = Map.of("x-requesting-bank", props.getRequestingBankId(), "x-consent-id",  consentService.getConsentForBank(bank_id, ConsentType.READ));
        var queryMap = Map.of("client_id", client_id);
        var response = wc.get(bank_id, ACCOUNTS_PATH, queryMap, headersMap, tokenService.getBankToken(bank_id), AccountsResponse.class);
        return response.accounts();
    }
    
    public TransactionsResponse getTransactions(String bank_id, String account_id, Map<String, String> queryMap) {
        var headersMap = Map.of("x-consent-id", consentService.getConsentForBank(bank_id, ConsentType.READ), "x-requesting-bank", props.getRequestingBankId());
        String path = ACCOUNTS_PATH + "/" + account_id + TRANSACTIONS_PATH;
        return wc.get(bank_id, path, queryMap, headersMap, tokenService.getBankToken(bank_id),
                TransactionsResponse.class);
    }
    
    public BigDecimal countGeneralExpens(String from_time, String to_time) {
        Map<String, String> queryMap = Map.of("from_booking_date_time", from_time, "to_booking_date_time", to_time);
        BigDecimal counter = new BigDecimal(0);
        List<BankClientPair> list = userService.getUserClientIds();
        for (var pair: list) {
            for(var account: getAccounts(pair.getBankId(), pair.getClientId())) {
                for(var transaction: getTransactions(pair.getBankId(), account.accountId(), queryMap).data().transaction()) {
                    if (transaction.status() == TransactionStatus.BOOKED 
                            && transaction.bankTransactionCode().code().contains("Issued"))
                        counter = counter.add(transaction.amount().amount());
                }
            }
        }
        return counter;
    }
     
    public BigDecimal countAccountExpens(String bank_id, String account_id, String from_time, String to_time) {
        Map<String, String> queryMap = Map.of("from_booking_date_time", from_time, "to_booking_date_time", to_time);
        BigDecimal counter = new BigDecimal(0);
        for (var transaction : getTransactions(bank_id, account_id, queryMap).data().transaction()) {
            if (transaction.status() == TransactionStatus.BOOKED
                    && transaction.bankTransactionCode().code().contains("Issued"))
                counter = counter.add(transaction.amount().amount());        }
        return counter;
    } 
    
    public List<Product> getAvailableProducts(String bank_id) {
        return wc.get(bank_id, AVAILABLE_PRODUCTS_PATH, null, null, tokenService.getBankToken(bank_id), AvailableProductsResponse.class).products();
    }
}
