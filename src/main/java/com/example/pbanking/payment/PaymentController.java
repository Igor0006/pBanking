package com.example.pbanking.payment;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.pbanking.payment.dto.request.MakePaymentRequest;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/payment")
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping()
    public ResponseEntity<String> makePayment(@RequestBody MakePaymentRequest request) {
        paymentService.makeSinglePayment(request);
        return ResponseEntity.ok().body("Payment created");
    }
}
