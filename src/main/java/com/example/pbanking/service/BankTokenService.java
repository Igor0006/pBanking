package com.example.pbanking.service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BankTokenService {
    private final WebClientExecutor wc;
    private final BankService bankService;
    private static final Duration SKEW = Duration.ofSeconds(90);
    
    private ConcurrentHashMap<String, Token> tokens = new ConcurrentHashMap<>();

    @Value("${bank.token_path}")
    private String path;
    @Value("${secret.team_code}")
    private String team_code;
    @Value("${secret.team_secret}")
    private String team_secret;
    
    public String getBankToken(String bank_id) {
        tokens.compute(bank_id, (k, existing) -> {
            if (isValid(existing)) return existing;
            return fetchToken(k);
        });
        return tokens.get(bank_id).value;
    }
    
    public Token fetchToken(String bank_id) {
        var form = Map.of(
                "client_id", team_code,
                "client_secret", team_secret
        );
        var response = wc.post(bank_id, path, null, form, null, null, TokenResponse.class);
        Token token = new Token(response.accessToken, Instant.now().plus(Duration.ofSeconds(response.expiresIn)));
        bankService.saveToken(bank_id, token.value(), token.expiresAt());
        return token;
    }
    
    private boolean isValid(Token t) {
        return t != null && Instant.now().plus(SKEW).isBefore(t.expiresAt());
    }
    
    private record Token(String value, Instant expiresAt) {}
    private record TokenResponse(
            @JsonProperty("access_token") String accessToken,
            @JsonProperty("expires_in") long expiresIn,
            @JsonProperty("token_type") String tokenType,
            @JsonProperty("client_id") String clientId
    ) {}

}
