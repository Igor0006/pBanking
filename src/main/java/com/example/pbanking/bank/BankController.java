package com.example.pbanking.bank;

import org.springframework.web.bind.annotation.RestController;

import com.example.pbanking.bank.dto.BankEntry;
import com.example.pbanking.bank.dto.response.BankProductResponse;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;


@RestController
@RequiredArgsConstructor
@RequestMapping("api/banks")
public class BankController {
    private final BankService bankService;
    private final ProductService productService;
    
    @GetMapping
    public ResponseEntity<List<BankEntry>> getAvailableBanks() {
        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(bankService.getAvailableBanks());
    }
    
    @GetMapping("/availableProducts/{bank_id}")
    public ResponseEntity<List<BankProductResponse>> getAvailableProducts(@PathVariable String bank_id) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(productService.getAvailableProducts(bank_id));
    }
    
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping("/addBank")
    public ResponseEntity<BankEntity> addBank(@RequestParam String bankId, @RequestParam String bankName, @RequestParam String url) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(bankService.createBank(bankId, bankName, url));
    }
}
    
