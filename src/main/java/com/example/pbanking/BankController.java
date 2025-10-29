package com.example.pbanking;

import org.springframework.web.bind.annotation.RestController;

import com.example.pbanking.model.AccountsResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;


@RestController
@RequiredArgsConstructor
public class BankController {
    private final WebClientExecutor wc;
    @GetMapping("/")
    public void getMethodName() {
        var response = wc.get("https://abank.open.bankingapi.ru/accounts?client_id=team062-1", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZWFtMDYyLTEiLCJ0eXBlIjoiY2xpZW50IiwiYmFuayI6InNlbGYiLCJleHAiOjE3NjE4MjIwNTl9.hgHkEPKlrVatqDzxcUG0ef4QOfFVAAX1qFTHvVUox1M\n" + //
                        "", 
            AccountsResponse.class);
        System.out.println(response);
    }
    
}
