package com.example.pbanking.conroller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.pbanking.dto.request.MultiPaymentConsetApiRequest;
import com.example.pbanking.dto.request.SinglePaymentConsentApiRequest;
import com.example.pbanking.dto.response.AccountConsentResponse;
import com.example.pbanking.model.enums.ConsentType;
import com.example.pbanking.service.ConsentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/consent")
public class ConsentController {
    private final ConsentService consentService;

    @PostMapping("/account")
    public ResponseEntity<String> getAccountConsent(@RequestBody AccountConsentApiRequest request) {
        consentService.getReadConsent(request.bank_id(), request.client_id());
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body("Consent created");
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


}
