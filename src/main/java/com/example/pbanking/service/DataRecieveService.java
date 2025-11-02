package com.example.pbanking.service;

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

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DataRecieveService {
    private final WebClientExecutor wc;
    private final BankTokenService tokenService;
    private final ConsentService consentService;
    private final TPPConfig props;
    private final static String ACCOUNTS_PATH = "/accounts";
    private final static String TRANSACTIONS_PATH = "/transactions";
    private final static String AVAILABLE_PRODUCTS_PATH = "/products";
    
    // take client_id in futire form security context 
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
    
    
    
    public List<Product> getAvailableProducts(String bank_id) {
        return wc.get(bank_id, AVAILABLE_PRODUCTS_PATH, null, null, tokenService.getBankToken(bank_id), AvailableProductsResponse.class).products();
    }
}
