package com.example.pbanking.payment;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.example.pbanking.account.AccountService;
import com.example.pbanking.bank.BankTokenService;
import com.example.pbanking.common.client.WebClientExecutor;
import com.example.pbanking.common.enums.ConsentType;
import com.example.pbanking.consent.CredentialsRepository;
import com.example.pbanking.consent.MultiPaymentConsentRepository;
import com.example.pbanking.consent.SinglePaymentConsentRepository;
import com.example.pbanking.consent.ConsentService;
import com.example.pbanking.exception.NotFoundException;
import com.example.pbanking.payment.dto.request.BankPaymentRequest;
import com.example.pbanking.payment.dto.request.MakePaymentRequest;
import com.example.pbanking.payment.dto.response.MakePaymentResponse;
import com.example.pbanking.user.User;
import com.example.pbanking.user.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final ConsentService consentService;
    private final BankTokenService tokenService;
    private final AccountService accountService;
    private final UserService userService;
    private final CredentialsRepository credentialsRepository;
    private final SinglePaymentConsentRepository singlePaymentConsentRepository;
    private final MultiPaymentConsentRepository multiPaymentConsentRepository;
    private final WebClientExecutor wc;
    private static final String PAYMENT_PATH = "/payments";
    private final static String defualtSchemeName = "RU.CBR.PAN";

    public MakePaymentResponse makeSinglePayment(MakePaymentRequest request) {
        User user = userService.getCurrentUser();

        checkBalance(request);

        String consent;
        HashMap<String, String> kwargs = new HashMap<String, String>(Map.of("debtorAccount", request.debtor_account(),
                "creditorAccount", request.creditor_account(),
                "amount", request.amount().toString()));

        try {
            consent = consentService.getConsentForBank(request.debtor_bank(), ConsentType.SINGLE_USE, kwargs);
        } catch (NotFoundException e) {
            return useMultiUseConsent(user, request);
        }

        String clientId = getClientId(user, request);
        BankPaymentRequest bankRequestBody = getBankPaymentRequest(request);
        MakePaymentResponse response = sendPaymentRequest(request, consent, bankRequestBody, clientId);

        singlePaymentConsentRepository.markAsUsed(user, request.debtor_account(), request.creditor_account(),
                request.amount());
        return response;
    }

    public MakePaymentResponse useMultiUseConsent(User user, MakePaymentRequest request) {
        HashMap<String, String> kwargs = new HashMap<String, String>(Map.of(
                "debtorAccount", request.debtor_account(),
                "amount", request.amount().toString()));

        String consent = consentService.getConsentForBank(request.debtor_bank(), ConsentType.MULTI_USE, kwargs);
        String clientId = getClientId(user, request);
        BankPaymentRequest bankRequestBody = getBankPaymentRequest(request);

        MakePaymentResponse response = sendPaymentRequest(request, consent, bankRequestBody, clientId);
        multiPaymentConsentRepository.markUsage(request.debtor_account(), request.amount());

        return response;
    }

    private MakePaymentResponse sendPaymentRequest(MakePaymentRequest request, String consent,
            BankPaymentRequest bankRequestBody, String clientId) {
        var headersMap = Map.of("x-payment-consent-id", consent);

        var queryMap = Map.of("client_id", clientId);

        return wc.post(request.debtor_bank(), PAYMENT_PATH, bankRequestBody, queryMap,
                headersMap,
                tokenService.getBankToken(request.debtor_bank()), MakePaymentResponse.class);
    }

    private void checkBalance(MakePaymentRequest request) {
        BigDecimal balance;
        try {
            balance = accountService.getAccountBalance(request.debtor_bank(), request.accountId());
        } catch (WebClientResponseException e) {
            if (isConsentRequired(e)) {
                throw new NotFoundException("Consent not found");
            }
            throw e;
        }
        if (request.amount().compareTo(balance) >= 1) {
            throw new RuntimeException("Insufficient funds on the balance sheet");
        }
    }

    private boolean isConsentRequired(WebClientResponseException exception) {
        if (exception == null || exception.getStatusCode() != HttpStatus.FORBIDDEN) {
            return false;
        }
        String body = exception.getResponseBodyAsString();
        return body != null && body.contains("\"error\":\"CONSENT_REQUIRED\"");
    }

    private BankPaymentRequest getBankPaymentRequest(MakePaymentRequest request) {
        BankPaymentRequest bankRequestBody = new BankPaymentRequest(
                request.amount(),
                request.currency(),
                request.debtor_account(),
                request.creditor_account(), request.debtor_scheme(), request.creditor_scheme(), request.comment());

        if (request.creditor_bank().isPresent()) {
            bankRequestBody.setCreditorBank(request.creditor_bank().get());
        }

        return bankRequestBody;
    }

    private String getClientId(User user, MakePaymentRequest request) {
        String clientId = credentialsRepository.findClientIdByUserAndBank(user.getId(), request.debtor_bank())
                .orElseThrow(() -> new NotFoundException(
                        "No client id for user: " + user.getUsername() + " and bank: " + request.debtor_bank()));

        return clientId;
    }

    // private PaymentStatus getPaymentStatus(String bankId, String paymentId,
    // String client_id) {
    // var headersMap = Map.of("x-consent-id",
    // consentService.getConsentForBank(bankId, ConsentType.READ),
    // "x-requesting-bank",
    // props.getRequestingBankId(), "client_id", client_id);

    // String path = PAYMENT_PATH + "/" + paymentId;
    // PaymentStatus response = wc.get(bankId, path, null,
    // headersMap, tokenService.getBankToken(bankId), PaymentStatus.class);
    // return response;
    // }
}
