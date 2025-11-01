package com.example.pbanking.service;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import com.example.pbanking.config.TPPConfig;
import com.example.pbanking.model.Consent;
import com.example.pbanking.model.User;
import com.example.pbanking.model.enums.Bank;
import com.example.pbanking.model.enums.ConsentType;
import com.example.pbanking.repository.ConsentRepository;
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

    public String getConsentForBank(String bankId, ConsentType consentType) {
        User user = userService.getCurrentUser();
        Consent consent = consentRepository
            .findByUserAndBankAndType(user, Bank.getBankFromCode(bankId), consentType)
            .orElseThrow(() -> new EntityNotFoundException("No such consent for bank: " + bankId));
        return encryptionService.decrypt(consent.getConsent());
    }

    private void saveConsents(AccountConsentResponse response, String bankId, ConsentType consentType) {
        User user = userService.getCurrentUser();
        Consent consent = new Consent();
        consent.setBank(Bank.getBankFromCode(bankId));
        consent.setConsent(encryptionService.encrypt(response.consent_id()));
        consent.setStatus(response.status());
        consent.setType(consentType);
        consent.setExpirationDate(LocalDateTime.now().plusDays(90));
        consent.setUser(user);
        consentRepository.save(consent);
    }
}
