package com.example.pbanking.consent;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Locale;
import java.util.Map;

import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.example.pbanking.bank.BankEntity;
import com.example.pbanking.bank.BankService;
import com.example.pbanking.bank.BankTokenService;
import com.example.pbanking.common.client.WebClientExecutor;
import com.example.pbanking.common.enums.ConsentStatus;
import com.example.pbanking.common.security.EncryptionService;
import com.example.pbanking.config.TPPConfig;
import com.example.pbanking.consent.dto.response.AccountConsentResponse;
import com.example.pbanking.consent.dto.response.CheckAccountConsentResponse;
import com.example.pbanking.exception.NotFoundException;
import com.example.pbanking.user.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConsentAccessService {

    private static final String CHECK_CONSENT_PATH = "/account-consents";

    private final BankService bankService;
    private final AccountConsentRepository accountConsentRepository;
    private final EncryptionService encryptionService;
    private final WebClientExecutor webClientExecutor;
    private final TPPConfig tppConfig;
    private final BankTokenService tokenService;
    
    @CachePut(cacheNames = "accountConsents", key = "#bankId + ':' + #user.id")
    public String saveAccountConsent(AccountConsentResponse response, String bankId, String clientId, User user) {
        BankEntity bank = bankService.getBankFromId(bankId);

        accountConsentRepository.findByUserAndBank(user, bank)
                .ifPresent(accountConsentRepository::delete);

        AccountConsent consent = new AccountConsent();
        String consentValue = Boolean.TRUE.equals(response.auto_approved()) ? response.consent_id()
                : response.request_id();

        if (consentValue != null) {
            consent.setConsent(encryptionService.encrypt(consentValue));
        }

        consent.setBank(bank);
        consent.setUser(user);
        consent.setStatus(ConsentStatus.valueOf(response.status().toLowerCase(Locale.ROOT)));
        consent.setExpirationDate(
                LocalDateTime.parse(response.created_at()).toInstant(ZoneOffset.UTC).plus(Duration.ofDays(90)));
        consent.setClientId(clientId);

        accountConsentRepository.save(consent);
        return consentValue;
    }

    @Cacheable(cacheNames = "accountConsents", key = "#bankId + ':' + #user.id")
    public String resolveAccountConsent(String bankId, User user) {
        BankEntity bank = bankService.getBankFromId(bankId);
        AccountConsent consent = accountConsentRepository.findByUserAndBank(user, bank)
                .orElseThrow(() -> new NotFoundException("Consent not found for bank: " + bank.getBankId()));
        return resolveConsentValue(consent, bank.getBankId());
    }

    private String resolveConsentValue(AccountConsent consent, String bankId) {
        if (consent.getStatus() == ConsentStatus.pending) {
            String pendingId = encryptionService.decrypt(consent.getConsent());
            String approvedConsent = checkConsentState(bankId, pendingId);
            if (approvedConsent == null) {
                throw new NotFoundException("Consent is not approved yet");
            }

            accountConsentRepository.delete(consent);
            AccountConsent approved = cloneApprovedConsent(consent, approvedConsent);
            accountConsentRepository.save(approved);
            return approvedConsent;
        }

        try {
            return encryptionService.decrypt(consent.getConsent());
        } catch (RuntimeException ex) {
            accountConsentRepository.delete(consent);
            throw new NotFoundException(
                    "Stored consent is invalid or expired for bank: " + bankId + ". Please request a new consent.");
        }
    }

    private AccountConsent cloneApprovedConsent(AccountConsent source, String approvedConsent) {
        AccountConsent clone = new AccountConsent();
        clone.setBank(source.getBank());
        clone.setUser(source.getUser());
        clone.setStatus(ConsentStatus.approved);
        clone.setExpirationDate(source.getExpirationDate());
        clone.setClientId(source.getClientId());
        clone.setConsent(encryptionService.encrypt(approvedConsent));
        return clone;
    }

    private String checkConsentState(String bankId, String requestId) {
        String path = CHECK_CONSENT_PATH + "/" + requestId;
        Map<String, String> headers = Map.of("x-fapi-interaction-id", tppConfig.getRequestingBankId());
        CheckAccountConsentResponse response = webClientExecutor.get(
                bankId,
                path,
                null,
                headers,
                tokenService.getBankToken(bankId),
                CheckAccountConsentResponse.class);

        CheckAccountConsentResponse.Data data = response.data();
        if (data != null && "Authorized".equalsIgnoreCase(data.status())) {
            return data.consentId();
        }
        return null;
    }
    
    
    
}
