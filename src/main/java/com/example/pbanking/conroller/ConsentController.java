package com.example.pbanking.conroller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.pbanking.dto.response.AccountConsentResponse;
import com.example.pbanking.service.ConsentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/consent")
public class ConsentController {
    private final ConsentService consentService;
    
    @PostMapping("/account")
    public ResponseEntity<AccountConsentResponse> getAccountConsent(@RequestBody AccountConsentApiRequest request) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(consentService.getReadConsent(request.bank_id, request.client_id));
    }
    public record AccountConsentApiRequest(String bank_id, String client_id) {}
    
    @PostMapping("/singlePayment")
    public String getSinglePaymentConsent(@RequestBody String entity) {        
        return entity;
    }
    
    @PostMapping("/multiplePayment")
    public String getMultiplePaymentConsent(@RequestBody String entity) {        
        return entity;
    }
  
}
