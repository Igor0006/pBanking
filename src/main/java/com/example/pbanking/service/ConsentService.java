package com.example.pbanking.service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.pbanking.model.Credentials;
import com.example.pbanking.config.TPPConfig;
import com.example.pbanking.model.Consent;
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
    private final ConsentRepository consentRepository;
    private final TPPConfig props;
    
    private final static String ACCOUNT_PATH = "/account-consents/request";

    public void getReadConsent(String bank_id, String client_id, String bank_token) {
        AccountConsentRequestBody requestBody = new AccountConsentRequestBody();
        requestBody.setClient_id(client_id);
        requestBody.setRequesting_bank(props.getRequestinBankId());
        requestBody.setRequesting_bank_name(props.getRequestinBankName());
    
        Map<String, String> headers = java.util.Collections.singletonMap("X-Requesting-Bank", props.getRequestinBankId());

        AccountConsentResponse response = wc.post(bank_id, ACCOUNT_PATH, requestBody, null, headers, bank_token, AccountConsentResponse.class);
        System.out.println(response);
        saveConsents(response, bank_id, ConsentType.READ);
    }

    /**
     * Finds the consent for the current user and requested bank
     * @return String consent
     */
    public String getConsentForBank(String bankId, ConsentType consentType) {
        User user = userService.getCurrentUser();
        Consent consent = consentRepository
            .findByUserAndBankAndType(user, Bank.getBankFromCode(bankId), consentType)
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
