package com.example.pbanking.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.pbanking.config.TPPConfig;
import com.example.pbanking.dto.AccountsResponse;
import com.example.pbanking.dto.AccountsResponse.Account;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DataRecieveService {
    private final WebClientExecutor wc;
    private final BankTokenService tokenService;
    private final TPPConfig props;
    private final static String ACCOUNTS_PATH = "/accounts";
    
    public List<Account> getAccounts(String bank_id) {
        var headersMap = Map.of("X-Requesting-Bank", props.getRequestinBankId(), " X-Consent-Id", "stab");
        var response = wc.get(bank_id, ACCOUNTS_PATH, null, headersMap, tokenService.getBankToken(bank_id), AccountsResponse.class);
        return response.accounts();
    }
}
