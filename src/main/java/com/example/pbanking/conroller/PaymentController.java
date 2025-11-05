package com.example.pbanking.conroller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.pbanking.dto.request.MakeSinglePaymentRequest;
import com.example.pbanking.service.PaymentService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/payment")
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping("/single")
    public ResponseEntity<String> makePayment(@RequestBody MakeSinglePaymentRequest request) {
        paymentService.makeSinglePayment(request);
        return ResponseEntity.ok().body("Payment created");
    }
}
