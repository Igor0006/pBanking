package com.example.pbanking.conroller;

import org.springframework.web.bind.annotation.RestController;

import com.example.pbanking.config.BanksProperties;

import com.example.pbanking.dto.AvailableProductsResponse.Product;
import com.example.pbanking.model.Credentials;
import com.example.pbanking.dto.BankEntry;
import com.example.pbanking.dto.MakePaymentResponse;
import com.example.pbanking.dto.MakeSinglePaymentRequest;
import com.example.pbanking.dto.PaymentStatus;
import com.example.pbanking.dto.SinglePaymentWithRecieverRequest;
import com.example.pbanking.dto.SinglePaymentWithoutRecieverRequest;
import com.example.pbanking.dto.TransactionsResponse;
import com.example.pbanking.service.BankTokenService;
import com.example.pbanking.service.ConsentService;
import com.example.pbanking.service.DataRecieveService;
import com.example.pbanking.service.PaymentService;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.bind.annotation.RequestMapping;



@RestController
@RequiredArgsConstructor
@RequestMapping("api/banks")
public class BankController {
    private final BanksProperties banks;
    private final ConsentService consentService;
    private final BankTokenService tokenService;
    private final PaymentService paymentService;
    private final DataRecieveService dataService;
    
    @GetMapping
    public ResponseEntity<List<BankEntry>> getAvailableBanks() {
        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(banks.getList());
    }
    
    @GetMapping("/availableProducts/{bank_id}")
    public ResponseEntity<List<Product>> getAvailableProducts(@PathVariable String bank_id) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(dataService.getAvailableProducts(bank_id));
    }
}
    
