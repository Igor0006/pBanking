package com.example.pbanking.service;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.pbanking.dto.BankPaymentRequest;
import com.example.pbanking.dto.request.MakeSinglePaymentRequest;
import com.example.pbanking.dto.response.MakePaymentResponse;
import com.example.pbanking.exception.NotFoundException;
import com.example.pbanking.model.User;
import com.example.pbanking.model.enums.ConsentType;
import com.example.pbanking.repository.CredentialsRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final ConsentService consentService;
    private final BankTokenService tokenService;
    private final AccountService accountService;
    private final UserService userService;
    private final CredentialsRepository credentialsRepository;
    private final WebClientExecutor wc;
    private static final String PAYMENT_PATH = "/payments";

    public MakePaymentResponse makeSinglePayment(MakeSinglePaymentRequest request) {
        User user = userService.getCurrentUser();

        String clientId = credentialsRepository.findClientIdByUserAndBank(user.getId(), request.debtor_bank())
                .orElseThrow(() -> new NotFoundException(
                        "No client id for user: " + user.getUsername() + " and bank: " + request.debtor_bank()));

        checkBalance(request);

        BankPaymentRequest bankRequestBody = new BankPaymentRequest(request.amount(), request.currency(),
                request.debtor_account(),
                request.creditor_account());

        if (request.creditor_bank().isPresent()) {
            bankRequestBody.setCreditorBank(request.creditor_bank().get());
        }

        String consent;
        try {
            consent = consentService.getConsentForBank(request.debtor_bank(), ConsentType.SINGLE_USE);
        } catch (NotFoundException e) {
            // поиск multi use consent
            throw new RuntimeException("fsdfsd");
        }

        var headersMap = Map.of("x-payment-consent-id", consent);

        var queryMap = Map.of("client_id", clientId);

        MakePaymentResponse response = wc.post(request.debtor_bank(), PAYMENT_PATH, bankRequestBody, queryMap,
                headersMap,
                tokenService.getBankToken(request.debtor_bank()), MakePaymentResponse.class);

        return response;
        // return getPaymentStatus(request.requesting_bank(),
        // response.data().paymentId(), clientId);
    }

    private void checkBalance(MakeSinglePaymentRequest request) {
        BigDecimal balance = accountService.getAccountBalance(request.debtor_bank(), request.accountId());
        if (request.amount().compareTo(balance) >= 1) {
            throw new RuntimeException("Insufficient funds on the balance sheet");
        }
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
