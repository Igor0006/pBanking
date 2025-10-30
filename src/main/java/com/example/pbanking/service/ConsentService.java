package com.example.pbanking.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import com.example.pbanking.config.BanksProperties;
import com.example.pbanking.model.AccountConsentRequestBody;
import com.example.pbanking.model.AccountConsentResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConsentService {
    private final WebClientExecutor wc;
    private final BanksProperties banks;
    
    @Value("${bank.id}")
    private String requesting_bank;

    @Value("${bank.name}")
    private String requesting_bank_name;

    public void getReadConsent(String bank_id, String client_id, String bank_token) {
        AccountConsentRequestBody requestBody = new AccountConsentRequestBody();
        requestBody.setClient_id(client_id);
        requestBody.setRequesting_bank(requesting_bank);
        requestBody.setRequesting_bank_name(requesting_bank_name);
    
        String base_url = banks.getUrlMap().get(bank_id);
        Map<String, String> headers = Map.of(
            "X-Requesting-Bank", requesting_bank,
            "Content-Type", MediaType.APPLICATION_JSON_VALUE
        );
        var response = wc.post(base_url, "/account-consents/request", requestBody, headers, bank_token, AccountConsentResponse.class);
        System.out.println(response);
    }
}
