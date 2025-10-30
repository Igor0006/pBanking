package com.example.pbanking.conroller;

import org.springframework.web.bind.annotation.RestController;

import com.example.pbanking.config.BanksProperties;
import com.example.pbanking.model.AccountsResponse;
import com.example.pbanking.service.ConsentService;
import com.example.pbanking.service.WebClientExecutor;
import com.fasterxml.jackson.core.JsonProcessingException;

import lombok.RequiredArgsConstructor;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;


@RestController
@RequiredArgsConstructor
public class BankController {
    private final WebClientExecutor wc;
    private final BanksProperties banks;
    private final ConsentService consentService;
    
    @GetMapping("/")
    public void getAccounts() {
        String path = "/accounts";

        Map<String, Object> queryParams = Map.of(
            "client_id", "team062-1"
        );

        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZWFtMDYyLTEiLCJ0eXBlIjoiY2xpZW50IiwiYmFuayI6InNlbGYiLCJleHAiOjE3NjE4MjIwNTl9.hgHkEPKlrVatqDzxcUG0ef4QOfFVAAX1qFTHvVUox1M";

        var response = wc.get(banks.getUrlMap().get("abank"), path, queryParams,null, token, AccountsResponse.class);
        System.out.println(response.getData().getAccount().get(0).getAccountId());
    }
    
    // не забыть нормально эндпоинт сделать
    @GetMapping("/account-consent")
    public void stabName() {
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZWFtMDYyIiwiY2xpZW50X2lkIjoidGVhbTA2MiIsInR5cGUiOiJ0ZWFtIiwiaXNzIjoiYWJhbmsiLCJhdWQiOiJvcGVuYmFua2luZyIsImV4cCI6MTc2MTkxNjgzNn0.Whz4kpWUyYDgbv232MCj4WF-iXDRirYLdasiWBdivT8";
        consentService.getReadConsent("abank", "team062-1", token);
    }
}
    
