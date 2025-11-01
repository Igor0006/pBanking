package com.example.pbanking.service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import com.example.pbanking.model.Credentials;
import com.example.pbanking.model.User;
import com.example.pbanking.model.enums.ConsentType;
import com.example.pbanking.repository.CredentialsRepository;
import com.example.pbanking.dto.AccountConsentRequestBody;
import com.example.pbanking.dto.AccountConsentResponse;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConsentService {
    private final WebClientExecutor wc;
    private final UserService userService;
    private final EncryptionService encryptionService;
    private final BankService bankService;
    private final CredentialsRepository credentialsRepository;
    
    @Value("${bank.id}")
    private String requesting_bank;

    @Value("${bank.name}")
    private String requesting_bank_name;

    public void getReadConsent(String bank_id, String client_id, String bank_token) {
        AccountConsentRequestBody requestBody = new AccountConsentRequestBody();
        requestBody.setClient_id(client_id);
        requestBody.setRequesting_bank(requesting_bank);
        requestBody.setRequesting_bank_name(requesting_bank_name);
    
        Map<String, String> headers = Map.of(
            "X-Requesting-Bank", requesting_bank,
            "Content-Type", MediaType.APPLICATION_JSON_VALUE
        );
        // var
        String path = "/account-consents/request";
        AccountConsentResponse response = wc.post(bank_id, path, requestBody, null, headers, bank_token, AccountConsentResponse.class);
        saveConsents(response, bank_id, ConsentType.READ);
    }

    /**
     * Finds the consent for the current user and requested bank
     * @return String consent
     */
    public String getConsentForBank(String bankId, ConsentType consentType) {
        User user = userService.getCurrentUser();
        Credentials consent = credentialsRepository
        .findByUserAndBankAndType(user, bankService.getBankFromId(bankId), consentType)
        .orElseThrow(() -> new EntityNotFoundException("No such consent for bank: " + bankId));
        return encryptionService.decrypt(consent.getConsent());
    }

    private void saveConsents(AccountConsentResponse response, String bankId, ConsentType consentType) {
        User user = userService.getCurrentUser();
        Credentials consent = new Credentials();
        consent.setBank(bankService.getBankFromId(bankId));
        consent.setConsent(encryptionService.encrypt(response.consent_id()));
        consent.setStatus(response.status());
        consent.setType(consentType);
        consent.setExpirationDate(Instant.now().plus(Duration.ofDays(90)));
        consent.setUser(user);
        credentialsRepository.save(consent);
    }
}
