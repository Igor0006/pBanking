package com.example.pbanking.service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.pbanking.config.TPPConfig;
import com.example.pbanking.dto.request.AccountConsentRequestBody;
import com.example.pbanking.dto.request.BasePaymentRequestBody;
import com.example.pbanking.dto.request.SinglePaymentWithRecieverRequest;
import com.example.pbanking.dto.request.SinglePaymentWithoutRecieverRequest;
import com.example.pbanking.dto.response.AccountConsentResponse;
import com.example.pbanking.dto.response.CheckAccountConsentResponse;
import com.example.pbanking.dto.response.PaymentConsentResponse;
import com.example.pbanking.exception.NotFoundException;
import com.example.pbanking.model.AccountConsent;
import com.example.pbanking.model.BankEntity;
import com.example.pbanking.model.SinglePaymentConsent;
import com.example.pbanking.model.User;
import com.example.pbanking.model.enums.ConsentStatus;
import com.example.pbanking.model.enums.ConsentType;
import com.example.pbanking.repository.AccountConsentRepository;
import com.example.pbanking.repository.SinglePaymentConsentRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConsentService {

    private static final String ACCOUNT_PATH = "/account-consents/request";
    private static final String CHECK_CONSENT_PATH = "/account-consents";
    private static final String PAYMENT_PATH = "/payment-consents/request";

    private final WebClientExecutor webClientExecutor;
    private final UserService userService;
    private final EncryptionService encryptionService;
    private final BankService bankService;
    private final AccountConsentRepository accountConsentRepository;
    private final SinglePaymentConsentRepository singlePaymentConsentRepository;
    private final TPPConfig tppConfig;
    private final BankTokenService tokenService;

    @Transactional
    public AccountConsentResponse getReadConsent(String bankId, String clientId) {
        AccountConsentRequestBody requestBody = new AccountConsentRequestBody();
        requestBody.setClient_id(clientId);
        requestBody.setRequesting_bank(tppConfig.getRequestingBankId());
        requestBody.setRequesting_bank_name(tppConfig.getRequestingBankName());

        Map<String, String> headers = new HashMap<>();
        if (tppConfig.getRequestingBankId() != null && !tppConfig.getRequestingBankId().isBlank()) {
            headers.put("X-Requesting-Bank", tppConfig.getRequestingBankId());
        }

        AccountConsentResponse response = webClientExecutor.post(
                bankId,
                ACCOUNT_PATH,
                requestBody,
                null,
                headers,
                tokenService.getBankToken(bankId),
                AccountConsentResponse.class);

        saveAccountConsent(response, bankId, clientId);
        return response;
    }

    @Transactional
    public String getPaymentConsent(String bankId, BasePaymentRequestBody requestBody) {
        Map<String, String> headers = new HashMap<>();
        if (requestBody.getRequesting_bank() != null && !requestBody.getRequesting_bank().isBlank()) {
            headers.put("X-Requesting-Bank", requestBody.getRequesting_bank());
        }

        PaymentConsentResponse response = webClientExecutor.post(
                bankId,
                PAYMENT_PATH,
                requestBody,
                null,
                headers,
                tokenService.getBankToken(bankId),
                PaymentConsentResponse.class);

        savePaymentConsent(response, requestBody, bankId);
        return response.consent_id();
    }

    public String getConsentForBank(String bankId, ConsentType consentType) {
        User user = userService.getCurrentUser();
        BankEntity bank = bankService.getBankFromId(bankId);

        return switch (consentType) {
            case READ -> resolveAccountConsent(bankId, user, bank);
            default -> throw new NotFoundException("Unsupported consent type: " + consentType);
        };
    }

    private String resolveAccountConsent(String bankId, User user, BankEntity bank) {
        AccountConsent consent = accountConsentRepository.findByUserAndBank(user, bank)
                .orElseThrow(() -> new NotFoundException("Consent not found for bank: " + bankId));
        return resolveConsentValue(consent, bankId);
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

    private void saveAccountConsent(AccountConsentResponse response, String bankId, String clientId) {
        User user = userService.getCurrentUser();
        BankEntity bank = bankService.getBankFromId(bankId);

        accountConsentRepository.findByUserAndBank(user, bank)
                .ifPresent(accountConsentRepository::delete);

        AccountConsent consent = new AccountConsent();
        String consentValue = Boolean.TRUE.equals(response.auto_approved()) ? response.consent_id() : response.request_id();

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
    }

    private void savePaymentConsent(PaymentConsentResponse response, BasePaymentRequestBody requestBody, String bankId) {
        ConsentType consentType = ConsentType.valueOf(response.consent_type().toUpperCase(Locale.ROOT));
        if (consentType == ConsentType.SINGLE_USE) {
            saveSinglePaymentConsent(response, requestBody, bankId);
            return;
        }

        throw new UnsupportedOperationException("Consent type " + consentType + " is not supported yet");
    }

    private void saveSinglePaymentConsent(PaymentConsentResponse response, BasePaymentRequestBody requestBody,
            String bankId) {
        User user = userService.getCurrentUser();
        BankEntity bank = bankService.getBankFromId(bankId);

        SinglePaymentConsent consent = new SinglePaymentConsent();
        consent.setConsent(encryptionService.encrypt(response.consent_id()));
        consent.setBank(bank);
        consent.setUser(user);
        consent.setClientId(requestBody.getClient_id());
        consent.setStatus(ConsentStatus.valueOf(response.status().toLowerCase(Locale.ROOT)));
        consent.setUsed(false);
        consent.setExpirationDate(Instant.parse(response.valid_until()));

        if (requestBody instanceof SinglePaymentWithRecieverRequest withReceiver) {
            consent.setCreditorAccount(withReceiver.getCreditor_account());
            consent.setDebtorAccount(withReceiver.getDebtor_account());
            consent.setAmount(withReceiver.getAmount());
        } else if (requestBody instanceof SinglePaymentWithoutRecieverRequest withoutReceiver) {
            consent.setDebtorAccount(withoutReceiver.getDebtor_account());
            consent.setAmount(withoutReceiver.getAmount());
        }

        singlePaymentConsentRepository.save(consent);
    }

    public String checkConsentState(String bankId, String requestId) {
        String path = CHECK_CONSENT_PATH + "/" + requestId;
        Map<String, String> headers = Map.of("x-fapi-interaction-id", tppConfig.getRequestingBankId());
        CheckAccountConsentResponse response = webClientExecutor.get(
                bankId,
                path,
                null,
                headers,
                null,
                CheckAccountConsentResponse.class);

        CheckAccountConsentResponse.Data data = response.data();
        if (data != null && "Authorized".equalsIgnoreCase(data.status())) {
            return data.consentId();
        }
        return null;
    }
}
