package com.example.pbanking.consent;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.pbanking.bank.BankEntity;
import com.example.pbanking.bank.BankService;
import com.example.pbanking.bank.BankTokenService;
import com.example.pbanking.common.client.WebClientExecutor;
import com.example.pbanking.common.enums.ConsentStatus;
import com.example.pbanking.common.enums.ConsentType;
import com.example.pbanking.common.security.EncryptionService;
import com.example.pbanking.config.TPPConfig;
import com.example.pbanking.consent.dto.request.AccountConsentRequestBody;
import com.example.pbanking.consent.dto.request.BasePaymentConsentApiRequest;
import com.example.pbanking.consent.dto.request.BasePaymentConsentRequestBody;
import com.example.pbanking.consent.dto.request.MultiPaymentConsentRequest;
import com.example.pbanking.consent.dto.request.MultiPaymentConsetApiRequest;
import com.example.pbanking.consent.dto.request.ProductConsentApiRequest;
import com.example.pbanking.consent.dto.request.ProductConsentRequest;
import com.example.pbanking.consent.dto.request.SinglePaymentConsentApiRequest;
import com.example.pbanking.consent.dto.request.SinglePaymentWithReceiverRequest;
import com.example.pbanking.consent.dto.response.AccountConsentResponse;
import com.example.pbanking.consent.dto.response.CheckAccountConsentResponse;
import com.example.pbanking.consent.dto.response.PaymentConsentResponse;
import com.example.pbanking.consent.AccountConsent;
import com.example.pbanking.consent.AccountConsentRepository;
import com.example.pbanking.consent.CredentialsRepository;
import com.example.pbanking.consent.MultiPaymentConsentRepository;
import com.example.pbanking.consent.ProductConsentRepository;
import com.example.pbanking.consent.SinglePaymentConsentRepository;
import com.example.pbanking.consent.MultiPaymentConsent;
import com.example.pbanking.consent.ProductConsent;
import com.example.pbanking.consent.SinglePaymentConsent;
import com.example.pbanking.exception.NotFoundException;
import com.example.pbanking.user.User;
import com.example.pbanking.user.UserService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConsentService {

    private static final String ACCOUNT_PATH = "/account-consents/request";
    private static final String CHECK_CONSENT_PATH = "/account-consents";
    private static final String PAYMENT_PATH = "/payment-consents/request";
    private static final String PRODUCT_PATH = "/product-agreement-consents/request";

    private final WebClientExecutor webClientExecutor;
    private final UserService userService;
    private final EncryptionService encryptionService;
    private final BankService bankService;
    private final CredentialsRepository credentialsRepository;
    private final AccountConsentRepository accountConsentRepository;
    private final SinglePaymentConsentRepository singlePaymentConsentRepository;
    private final MultiPaymentConsentRepository multiPaymentConsentRepository;
    private final ProductConsentRepository productConsentRepository;
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
    public PaymentConsentResponse getPaymentConsent(BasePaymentConsentApiRequest requestBody, ConsentType consentType) {
        Map<String, String> headers = new HashMap<>();
        headers.put("X-Requesting-Bank", tppConfig.getRequestingBankId());

        User user = userService.getCurrentUser();

        String clientId = credentialsRepository.findClientIdByUserAndBank(user.getId(), requestBody.getBank_id())
                .orElseThrow(() -> new NotFoundException(
                        "No client id for user: " + user.getUsername() + " and bank: " + requestBody.getBank_id()));

        BasePaymentConsentRequestBody bankRequest = switch (consentType) {
            case SINGLE_USE -> {
                SinglePaymentConsentApiRequest single = (SinglePaymentConsentApiRequest) requestBody;
                yield new SinglePaymentWithReceiverRequest(tppConfig.getRequestingBankId(), clientId,
                        consentType.toString().toLowerCase(Locale.ROOT), single.getAmount(), single.getCurrency(),
                        single.getDebtor_account(),
                        single.getCreditor_account(), single.getCreditor_name(), single.getReference());
            }
            case MULTI_USE -> {
                MultiPaymentConsetApiRequest multi = (MultiPaymentConsetApiRequest) requestBody;
                yield new MultiPaymentConsentRequest(tppConfig.getRequestingBankId(), clientId,
                        consentType.toString().toLowerCase(Locale.ROOT), multi.getDebtor_account(), multi.getMax_uses(),
                        multi.getMax_amount_per_payment(), multi.getMax_total_amount(), multi.getValid_until());
            }
            default -> throw new NotFoundException("Not a payment consent: " + consentType);
        };

        PaymentConsentResponse response = webClientExecutor.post(
                requestBody.getBank_id(),
                PAYMENT_PATH,
                bankRequest,
                null,
                headers,
                tokenService.getBankToken(requestBody.getBank_id()),
                PaymentConsentResponse.class);

        savePaymentConsent(response, requestBody, clientId);
        return response;
    }

    public void getProductConsent(ProductConsentApiRequest requestBody) {
        User user = userService.getCurrentUser();

        String clientId = credentialsRepository.findClientIdByUserAndBank(user.getId(), requestBody.bank_id())
                .orElseThrow(() -> new NotFoundException(
                        "No client id for user: " + user.getUsername() + " and bank: " + requestBody.bank_id()));

        var queryMap = Map.of("client_id", clientId);

        ProductConsentRequest bankRequest = new ProductConsentRequest(tppConfig.getRequestingBankId(),
                clientId, requestBody.read_product_agreements(), requestBody.open_product_agreements(),
                requestBody.close_product_agreements(), requestBody.allowed_product_types(), requestBody.max_amount(),
                requestBody.valid_until(), "Финансовый агрегатор для управления продуктами");

        Object response = webClientExecutor.post(requestBody.bank_id(),
                PRODUCT_PATH,
                bankRequest,
                queryMap,
                null,
                tokenService.getBankToken(requestBody.bank_id()),
                Object.class);

        saveProductConsent(response, requestBody, clientId);

    }

    public String getConsentForBank(String bankId, ConsentType consentType, Map<String, String> kwargs) {
        User user = userService.getCurrentUser();
        return switch (consentType) {
            case READ -> resolveAccountConsent(bankId, user);
            case SINGLE_USE -> resolveSingPaymentConsent(bankId, user, kwargs);
            case MULTI_USE -> resolveMultiPaymentConsent(bankId, user, kwargs);
            // case PRODUCT_READ -> resolveProductReadConsent(bankId, user, kwargs);
            // case PRODUCT_OPEN -> resolveProductOpenConsent(bankId, user, kwargs);
            // case PRODUCT_CLOSE -> resolveProductCloseConsent(bankId, user, kwargs);
            default -> throw new NotFoundException("Unsupported consent type: " + consentType);
        };
    }

    private String resolveAccountConsent(String bankId, User user) {
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

    private void saveAccountConsent(AccountConsentResponse response, String bankId, String clientId) {
        User user = userService.getCurrentUser();
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
    }

    private String resolveSingPaymentConsent(String bankId, User user, Map<String, String> kwargs) {
        SinglePaymentConsent consent = singlePaymentConsentRepository
                .findAppropriateConsent(
                        user,
                        kwargs.get("debtorAccount"),
                        kwargs.get("creditorAccount"),
                        new BigDecimal(kwargs.get("amount")),
                        Instant.now())
                .orElseThrow(() -> new NotFoundException("Consent not found for debtor account: "
                        + kwargs.get("debtorAccount") + " and creditor account: " + kwargs.get("creditorAccount")));
        return resolveSinglePaymentConsentValue(consent, bankId);
    }

    private String resolveSinglePaymentConsentValue(SinglePaymentConsent consent,
            String bankId) {
        if (consent.getStatus() == ConsentStatus.pending) {
            String pendingId = encryptionService.decrypt(consent.getConsent());
            String approvedConsent = checkPaymentConsentState(bankId, pendingId);
            if (approvedConsent == null) {
                throw new NotFoundException("Consent is not approved yet");
            }

            consent.setConsent(approvedConsent);
            singlePaymentConsentRepository.save(consent);
            return approvedConsent;
        }

        try {
            return encryptionService.decrypt(consent.getConsent());
        } catch (RuntimeException ex) {
            singlePaymentConsentRepository.delete(consent);
            throw new NotFoundException(
                    "Stored consent is invalid or expired for bank: " + bankId + ". Please request a new consent.");
        }
    }

    private void savePaymentConsent(PaymentConsentResponse response, BasePaymentConsentApiRequest requestBody,
            String clientId) {
        ConsentType consentType = ConsentType.valueOf(response.consent_type().toUpperCase(Locale.ROOT));
        if (consentType == ConsentType.SINGLE_USE) {
            saveSinglePaymentConsent(response, requestBody, clientId);
            return;
        } else if (consentType == ConsentType.MULTI_USE) {
            saveMultiPaymentConsent(response, requestBody, clientId);
            return;
        }

        throw new UnsupportedOperationException("Consent type " + consentType + " is not supported yet");
    }

    private void saveSinglePaymentConsent(PaymentConsentResponse response, BasePaymentConsentApiRequest requestBody,
            String clientId) {
        if (!(requestBody instanceof SinglePaymentConsentApiRequest single)) {
            throw new RuntimeException("Not a SinglePaymentConsentApiRequest: " + response.consent_type());
        }

        User user = userService.getCurrentUser();
        BankEntity bank = bankService.getBankFromId(requestBody.getBank_id());

        singlePaymentConsentRepository
                .findAppropriateConsent(user, single.getDebtor_account(), single.getCreditor_account(),
                        single.getAmount(), Instant.now())
                .ifPresent(singlePaymentConsentRepository::delete);

        SinglePaymentConsent consent = new SinglePaymentConsent();
        String consentValue = Boolean.TRUE.equals(response.auto_approved()) ? response.consent_id()
                : response.request_id();
        if (consentValue != null) {
            consent.setConsent(encryptionService.encrypt(consentValue));
        }

        consent.setBank(bank);
        consent.setUser(user);
        consent.setClientId(clientId);
        consent.setStatus(ConsentStatus.valueOf(response.status().toLowerCase(Locale.ROOT)));
        consent.setUsed(false);
        consent.setExpirationDate(Instant.parse(response.valid_until()));
        consent.setCreditorAccount(single.getCreditor_account());
        consent.setDebtorAccount(single.getDebtor_account());
        consent.setAmount(single.getAmount());

        singlePaymentConsentRepository.save(consent);
    }

    public void saveMultiPaymentConsent(PaymentConsentResponse response, BasePaymentConsentApiRequest requestBody,
            String clientId) {
        if (!(requestBody instanceof MultiPaymentConsetApiRequest multi)) {
            throw new RuntimeException("Not a MultiPaymentConsetApiRequest: " + response.consent_type());
        }

        User user = userService.getCurrentUser();
        BankEntity bank = bankService.getBankFromId(requestBody.getBank_id());

        MultiPaymentConsent consent = new MultiPaymentConsent();
        String consentValue = Boolean.TRUE.equals(response.auto_approved()) ? response.consent_id()
                : response.request_id();
        if (consentValue != null) {
            consent.setConsent(encryptionService.encrypt(consentValue));
        }

        consent.setBank(bank);
        consent.setUser(user);
        consent.setClientId(clientId);
        consent.setStatus(ConsentStatus.valueOf(response.status().toLowerCase(Locale.ROOT)));
        consent.setDebtorAccount(multi.getDebtor_account());
        consent.setMaxAmountPerPayment(multi.getMax_amount_per_payment());
        consent.setExpirationDate(Instant.parse(response.valid_until()));
        consent.setMaxTotalAmount(multi.getMax_total_amount());
        consent.setMaxUses(multi.getMax_uses());

        multiPaymentConsentRepository.save(consent);
    }

    private String resolveMultiPaymentConsent(String bankId, User user, Map<String, String> kwargs) {
        MultiPaymentConsent consent = multiPaymentConsentRepository
                .findAppropriateConsent(
                        user,
                        kwargs.get("debtorAccount"),
                        new BigDecimal(kwargs.get("amount")),
                        Instant.now())
                .orElseThrow(() -> new NotFoundException("Consent not found for debtor account: "
                        + kwargs.get("debtorAccount") + " and creditor account: " + kwargs.get("creditorAccount")));
        return resolveMultiPaymentConsentValue(consent, bankId);
    }

    private String resolveMultiPaymentConsentValue(MultiPaymentConsent consent, String bankId) {
        if (consent.getStatus() == ConsentStatus.pending) {
            String pendingId = encryptionService.decrypt(consent.getConsent());
            String approvedConsent = checkPaymentConsentState(bankId, pendingId);
            if (approvedConsent == null) {
                throw new NotFoundException("Consent is not approved yet");
            }

            consent.setConsent(approvedConsent);
            multiPaymentConsentRepository.save(consent);
            return approvedConsent;
        }

        try {
            return encryptionService.decrypt(consent.getConsent());
        } catch (RuntimeException ex) {
            multiPaymentConsentRepository.delete(consent);
            throw new NotFoundException(
                    "Stored consent is invalid or expired for bank: " + bankId + ". Please request a new consent.");
        }
    }

    private void saveProductConsent(Object response, ProductConsentApiRequest requestBody, String clientId) {
        ProductConsent consent = new ProductConsent();
        User user = userService.getCurrentUser();
        BankEntity bank = bankService.getBankFromId(requestBody.bank_id());

        consent.setBank(bank);
        consent.setUser(user);
        // String consentValue = Boolean.TRUE.equals(response.auto_approved()) ?
        // response.consent_id()
        // : response.request_id();
        // if (consentValue != null) {
        // consent.setConsent(encryptionService.encrypt(consentValue));
        // }
        // consent.setStatus(response.status());
        consent.setAllowedProductTypes(requestBody.allowed_product_types());
        consent.setReadProductAgreements(requestBody.read_product_agreements());
        consent.setOpenProductAgreements(requestBody.open_product_agreements());
        consent.setCloseProductAgreements(requestBody.close_product_agreements());
        consent.setMaxAmount(requestBody.max_amount());
        consent.setClientId(clientId);
        consent.setExpirationDate(Instant.parse(requestBody.valid_until()));

        productConsentRepository.save(consent);
    }

    // private String resolveProductReadConsent(String bankId, User user, Map<String, String> kwargs) {
    //     BankEntity bank = bankService.getBankFromId(bankId);
    //     ProductConsent consent = productConsentRepository.findProductReadConsent(user, bank, kwargs.get("productType"))
    //             .orElseThrow(() -> new NotFoundException("No read product consent for user: " + user.getUsername()
    //                     + ", bank: " + bankId + " and product type: " + kwargs.get("productType")));

    //     return resolveProductConsentValue(consent, bankId);
    // }

    // private String resolveProductOpenConsent(String bankId, User user, Map<String, String> kwargs) {
    //     BankEntity bank = bankService.getBankFromId(bankId);
    //     ProductConsent consent = productConsentRepository.findProductOpenConsent(user, bank, kwargs.get("productType"))
    //             .orElseThrow(() -> new NotFoundException("No read product consent for user: " + user.getUsername()
    //                     + ", bank: " + bankId + " and product type: " + kwargs.get("productType")));

    //     return resolveProductConsentValue(consent, bankId);
    // }

    // private String resolveProductCloseConsent(String bankId, User user, Map<String, String> kwargs) {
    //     BankEntity bank = bankService.getBankFromId(bankId);
    //     ProductConsent consent = productConsentRepository.findProductCloseConsent(user, bank, kwargs.get("productType"))
    //             .orElseThrow(() -> new NotFoundException("No read product consent for user: " + user.getUsername()
    //                     + ", bank: " + bankId + " and product type: " + kwargs.get("productType")));

    //     return resolveProductConsentValue(consent, bankId);
    // }

    // private String resolveProductConsentValue(ProductConsent consent, String bankId) {
    //     if (consent.getStatus() == ConsentStatus.pending) {
    //         String pendingId = encryptionService.decrypt(consent.getConsent());
    //         String approvedConsent = checkProductConsentState(bankId, pendingId);
    //         if (approvedConsent == null) {
    //             throw new NotFoundException("Consent is not approved yet");
    //         }

    //         consent.setConsent(approvedConsent);
    //         productConsentRepository.save(consent);
    //         return approvedConsent;
    //     }

    //     try {
    //         return encryptionService.decrypt(consent.getConsent());
    //     } catch (RuntimeException ex) {
    //         throw new NotFoundException(
    //                 "Stored consent is invalid or expired for bank: " + bankId + ". Please request a new consent.");
    //     }
    // }

    public String checkPaymentConsentState(String bankId, String requestId) {
        return requestId;
    }

    // private String checkProductConsentState(String bankId, String requestId) {
    //     return requestId;
    // }

    public String checkConsentState(String bankId, String requestId) {
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
