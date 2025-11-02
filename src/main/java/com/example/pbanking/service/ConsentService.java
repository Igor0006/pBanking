package com.example.pbanking.service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.pbanking.model.Credentials;
import com.example.pbanking.config.TPPConfig;
import com.example.pbanking.model.User;
import com.example.pbanking.model.enums.ConsentStatus;
import com.example.pbanking.model.enums.ConsentType;
import com.example.pbanking.repository.CredentialsRepository;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    private final TPPConfig props;
    private final BankTokenService tokenService;
    
    private final static String ACCOUNT_PATH = "/account-consents/request";
    private final static String CHECK_CONSENT_PATH = "/account-consents";

    public AccountConsentResponse getReadConsent(String bank_id, String client_id) {
        AccountConsentRequestBody requestBody = new AccountConsentRequestBody();
        requestBody.setClient_id(client_id);
        requestBody.setRequesting_bank(props.getRequestingBankId());
        requestBody.setRequesting_bank_name(props.getRequestingBankName());
    
        Map<String, String> headers = Map.of("X-Requesting-Bank", props.getRequestingBankId());

        AccountConsentResponse response = wc.post(bank_id, ACCOUNT_PATH, requestBody, null, headers, tokenService.getBankToken(bank_id), AccountConsentResponse.class);
        saveConsents(response, bank_id, client_id, ConsentType.READ);
        return response;
    }

    /**
     * Finds the consent for the current user and requested bank
     * @return String consent
     */
    
     //add client id support
    public String getConsentForBank(String bankId, ConsentType consentType) {
        User user = userService.getCurrentUser();
        Credentials consent = credentialsRepository.findByUserAndBankAndType(user, bankService.getBankFromId(bankId), consentType)
                .orElseThrow(() -> new EntityNotFoundException("No such consent for bank: " + bankId));

        if (consent.getStatus().equals(ConsentStatus.pending)) {
            String pendingId = encryptionService.decrypt(consent.getConsent());
            String approvedConsent = checkConsentState(bankId, pendingId);
            if (approvedConsent != null) {
                credentialsRepository.delete(consent);

                Credentials approved = new Credentials();
                approved.setConsent(encryptionService.encrypt(approvedConsent));
                approved.setBank(consent.getBank());
                approved.setStatus(ConsentStatus.approved);
                approved.setType(consentType);
                approved.setExpirationDate(consent.getExpirationDate());
                approved.setUser(consent.getUser());
                approved.setClientId(consent.getClientId());

                credentialsRepository.save(approved);
                return approvedConsent;
            }
            throw new EntityNotFoundException("Consent is not aprroved yet");
        }
        try {
            return encryptionService.decrypt(consent.getConsent());
        } catch (IllegalStateException ex) {
            credentialsRepository.delete(consent);
            throw new EntityNotFoundException("Stored consent is invalid or expired for bank: " + bankId + ". Please request a new consent.");
        }
    }
    
    public String checkConsentState(String bank_id, String request_id) {
        String path = CHECK_CONSENT_PATH + "/" + request_id;
        var headers = Map.of("x-fapi-interaction-id", props.getRequestingBankId());
        var response = wc.get(bank_id, path, null, headers, null, CheckAccountConsentResponse.class);
        var d = response.data();
        if ("Authorized".equalsIgnoreCase(d.status())) {
            return d.consentId();
        }
        return null;
    }

    private void saveConsents(AccountConsentResponse response, String bankId, String clientId, ConsentType consentType) {
        User user = userService.getCurrentUser();
        credentialsRepository.findByUserAndBankAndClientId(user, bankService.getBankFromId(bankId), clientId)
                .ifPresent(credentialsRepository::delete);

        Credentials consent = new Credentials();
        
        if(response.auto_approved()) {
            consent.setConsent(encryptionService.encrypt(response.consent_id()));
        } else {
            consent.setConsent(encryptionService.encrypt(response.request_id()));
        }
        consent.setBank(bankService.getBankFromId(bankId));
        consent.setStatus(ConsentStatus.valueOf(response.status().toLowerCase(Locale.ROOT)));
        consent.setType(consentType);
        consent.setExpirationDate(LocalDateTime.parse(response.created_at()).toInstant(ZoneOffset.UTC).plus(Duration.ofDays(90)));
        consent.setUser(user);
        consent.setClientId(clientId);
        credentialsRepository.save(consent);
    }
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CheckAccountConsentResponse(
            @JsonProperty("data") Data data
    ) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        public record Data(
                @JsonProperty(required = false) String consentId,
                String status,
                Instant creationDateTime,
                Instant statusUpdateDateTime,
                List<String> permissions,
                Instant expirationDateTime
        ) {}
    }
}
