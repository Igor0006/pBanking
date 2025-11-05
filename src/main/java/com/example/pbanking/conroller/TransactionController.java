package com.example.pbanking.conroller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.pbanking.dto.request.UpdateTypeRequest;
import com.example.pbanking.service.TransactionService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequiredArgsConstructor
@RequestMapping("api/transaction")
public class TransactionController {
    private final TransactionService transactionService;
    
    @PostMapping("/setType")
    @Secured("ROLE_PREMIUM")
    public ResponseEntity postMethodName(@RequestBody UpdateTypeRequest request) {
        transactionService.setTypeForTransaction(request.transaction_id(), request.type());
        return ResponseEntity.status(200).body(null);    
    }
    
}
