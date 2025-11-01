package com.example.pbanking.conroller;

import org.springframework.web.bind.annotation.RestController;

import com.example.pbanking.config.BanksProperties;
import com.example.pbanking.dto.AccountsResponse;
import com.example.pbanking.dto.AccountsResponse.Account;
import com.example.pbanking.dto.BankEntry;
import com.example.pbanking.service.BankTokenService;
import com.example.pbanking.service.ConsentService;
import com.example.pbanking.service.DataRecieveService;
import com.example.pbanking.service.WebClientExecutor;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;




@RestController
@RequiredArgsConstructor
public class BankController {
    private final BanksProperties banks;
    private final ConsentService consentService;
    private final BankTokenService tokenService;
    private final DataRecieveService dataService;
    
    @GetMapping("/api/banks")
    public ResponseEntity<List<BankEntry>> getMethodName() {
        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(banks.getList());
    }
    
    @GetMapping("/api/accounts/{bank_id}")
    public ResponseEntity<List<Account>> getMethodName(@PathVariable String bank_id) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(dataService.getAccounts(bank_id));
    }
    
    
    
    // не забыть нормально эндпоинт сделать
    @GetMapping("/account-consent")
    public void stabName() {
        String token = tokenService.getBankToken("abank");
        consentService.getReadConsent("abank", "team062-1", token);
    }
}
    
