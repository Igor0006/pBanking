package com.example.pbanking.service;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.pbanking.config.TPPConfig;
import com.example.pbanking.dto.BalanceResponse;
import com.example.pbanking.dto.IntrabankPayment;
import com.example.pbanking.dto.MakePaymentResponse;
import com.example.pbanking.dto.MakeSinglePaymentRequest;
import com.example.pbanking.dto.PaymentStatus;
import com.example.pbanking.dto.SinglePaymentWithRecieverRequest;
import com.example.pbanking.model.enums.ConsentType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final ConsentService consentService;
    private final BankTokenService tokenService;
    private final DataRecieveService dataRecieveService;
    private final WebClientExecutor wc;
    private static final String PAYMENT_PATH = "/payments";
    private final TPPConfig props;

    public MakePaymentResponse makeSinglePayment(MakeSinglePaymentRequest request, String clientId) {
        checkBalance(request);
        IntrabankPayment body = new IntrabankPayment(request.amount(), request.currency(), request.debtor_account(),
                request.creditor_account());
        if (request.creditor_bank().isPresent()) {
            body.setCreditorBank(request.creditor_bank().get());
        }

        SinglePaymentWithRecieverRequest consentRequest = new SinglePaymentWithRecieverRequest(
                request.requesting_bank(), clientId, ConsentType.SINGLE_USE.toString().toLowerCase(), request.amount(),
                request.debtor_account(), request.creditor_account(), request.creditor_name(), request.reference());

        var headersMap = Map.of("x-payment-consent-id",
                consentService.getPaymentConsent(request.requesting_bank(), consentRequest));

        var queryMap = Map.of("client_id", clientId);

        MakePaymentResponse response = wc.post(request.requesting_bank(), PAYMENT_PATH, body, queryMap, headersMap,
                tokenService.getBankToken(request.requesting_bank()), MakePaymentResponse.class);

        return response;
        // return getPaymentStatus(request.requesting_bank(), response.data().paymentId(), clientId);
    }

    private void checkBalance(MakeSinglePaymentRequest request) {
        BigDecimal balance = dataRecieveService.getAccountBalance(request.requesting_bank(), request.accountId());
        if (request.amount().compareTo(balance) >= 1) {
            throw new RuntimeException("Insufficient funds on the balance sheet");
        }
    }

    // private PaymentStatus getPaymentStatus(String bankId, String paymentId, String client_id) {
    //     var headersMap = Map.of("x-consent-id",
    //             consentService.getConsentForBank(bankId, ConsentType.READ), "x-requesting-bank",
    //             props.getRequestingBankId(), "client_id", client_id);



    //     String path = PAYMENT_PATH + "/" + paymentId;
    //     PaymentStatus response = wc.get(bankId, path, null,
    //             headersMap, tokenService.getBankToken(bankId), PaymentStatus.class);
    //     return response;
    // }
}
