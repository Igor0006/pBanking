package com.example.pbanking.bank;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.pbanking.bank.BankService.StoredToken;
import com.example.pbanking.common.client.WebClientExecutor;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BankTokenService {
    private final WebClientExecutor wc;
    private final BankService bankService;
    private static final Duration SKEW = Duration.ofSeconds(90);
    
    private ConcurrentHashMap<String, Token> tokens = new ConcurrentHashMap<>();

    private final static String path = "/auth/bank-token";
    
    @Value("${secret.team_code}")
    private String team_code;
    @Value("${secret.team_secret}")
    private String team_secret;
    
    public String getBankToken(String bank_id) {
        tokens.compute(bank_id, (k, existing) -> {
            if (isValid(existing)) {
                return existing;
            }
            Token stored = loadFromStorage(k);
            if (isValid(stored)) {
                return stored;
            }
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

    private boolean isValid(StoredToken stored) {
        return stored != null && Instant.now().plus(SKEW).isBefore(stored.expiresAt());
    }
    
    private Token loadFromStorage(String bankId) {
        return bankService.getStoredToken(bankId)
                .filter(this::isValid)
                .map(stored -> new Token(stored.value(), stored.expiresAt()))
                .orElse(null);
    }
    
    
    private record Token(String value, Instant expiresAt) {}
    private record TokenResponse(
            @JsonProperty("access_token") String accessToken,
            @JsonProperty("expires_in") long expiresIn,
            @JsonProperty("token_type") String tokenType,
            @JsonProperty("client_id") String clientId
    ) {}

}
