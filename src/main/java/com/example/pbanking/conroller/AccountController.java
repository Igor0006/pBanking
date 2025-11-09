package com.example.pbanking.conroller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.pbanking.dto.AccountSummary;
import com.example.pbanking.dto.request.UpdateDescriptionRequest;
import com.example.pbanking.dto.request.UpdateTypeRequest;
import com.example.pbanking.service.AccountService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequiredArgsConstructor
@RequestMapping("api/account")
public class AccountController {
    private final AccountService accountService;
    
    
    @GetMapping("/getAccounts/{bank_id}/{client_id}")
    public ResponseEntity<List<AccountSummary>> getUserBankAccounts(@PathVariable String bank_id, @PathVariable String client_id) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(accountService.getAccountsPrime(bank_id, client_id));
    }
    
    @PostMapping("/setType")
    public ResponseEntity<Void> setAccountType(@RequestBody UpdateTypeRequest request) {    
        accountService.setTypeForAccount(request.bankId(), request.id(), request.type());    
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(null);
    }
    
    @PostMapping("/setDescription")
    public ResponseEntity<Void> setAccountDescription(@RequestBody UpdateDescriptionRequest request) {
        accountService.setDescription(request.bankId(), request.id(), request.text());
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(null);
    }
    
}
