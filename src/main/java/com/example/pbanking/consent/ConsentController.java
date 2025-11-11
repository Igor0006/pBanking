package com.example.pbanking.consent;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.pbanking.consent.dto.request.MultiPaymentConsetApiRequest;
import com.example.pbanking.consent.dto.request.SinglePaymentConsentApiRequest;
import com.example.pbanking.consent.dto.response.AccountConsentResponse;
import com.example.pbanking.common.enums.ConsentType;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/consent")
public class ConsentController {
    private final ConsentService consentService;

    @PostMapping("/account")
    public ResponseEntity<AccountConsentResponse> getAccountConsent(@RequestBody AccountConsentApiRequest request) {
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(consentService.getReadConsent(request.bank_id, request.client_id));
    }

    public record AccountConsentApiRequest(String bank_id, String client_id) {
    }

    @PostMapping("/singlePayment")
    public ResponseEntity<String> getSinglePaymentConsent(@RequestBody SinglePaymentConsentApiRequest request) {
        consentService.getPaymentConsent(request, ConsentType.SINGLE_USE);
        return ResponseEntity.status(HttpStatus.CREATED).body("Consent created");
    }

    @PostMapping("/multiplePayment")
    public ResponseEntity<String> getMultiplePaymentConsent(@RequestBody MultiPaymentConsetApiRequest  request) {
        consentService.getPaymentConsent(request, ConsentType.MULTI_USE);
        return ResponseEntity.status(HttpStatus.CREATED).body("Consent created");
    }

    // @PostMapping("/product")
    // public ResponseEntity<String> getProductConsent(@RequestBody ProductConsentApiRequest request) {
    //     consentService.getProductConsent(request);
    //     return ResponseEntity.status(HttpStatus.CREATED).body("Consent created");
    // }

}
