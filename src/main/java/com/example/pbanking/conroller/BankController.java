package com.example.pbanking.conroller;

import org.springframework.web.bind.annotation.RestController;

import com.example.pbanking.config.BanksProperties;
import com.example.pbanking.dto.AccountsResponse.Account;
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
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;




@RestController
@RequiredArgsConstructor
public class BankController {
    private final BanksProperties banks;
    private final ConsentService consentService;
    private final BankTokenService tokenService;
    private final PaymentService paymentService;
    private final DataRecieveService dataService;
    
    @GetMapping("/api/banks")
    public ResponseEntity<List<BankEntry>> getAvailableBanks() {
        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(banks.getList());
    }
    
    @GetMapping("/api/availableProducts/{bank_id}")
    public ResponseEntity<List<Product>> getAvailableProducts(@PathVariable String bank_id) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(dataService.getAvailableProducts(bank_id));
    }
    
    
    @GetMapping("/api/accounts/{bank_id}")
    public ResponseEntity<List<Account>> getUserBankAccounts(@PathVariable String bank_id) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(dataService.getAccounts(bank_id, "team062-1"));
    }
    
    @GetMapping("/api/transactions/{bank_id}/{account_id}")
    public ResponseEntity<TransactionsResponse> getMethodName(@PathVariable String bank_id, @PathVariable String account_id, @RequestParam(required = false) String from_booking_date_time,
                                @RequestParam(required = false) String to_booking_date_time, @RequestParam(defaultValue = "1") String page, 
                                @RequestParam(defaultValue = "50") String limit) {
        
        Map<String, String> queryMap = Map.of("page", page, "limit", limit);
        if (from_booking_date_time != null) queryMap.put("from_booking_date_time", from_booking_date_time);
        if (to_booking_date_time != null) queryMap.put("to_booking_date_time", to_booking_date_time);
                                
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(dataService.getTransactions(bank_id, account_id, queryMap));
    }
    
    
    // не забыть нормально эндпоинт сделать
    @GetMapping("/account-consent")
    public String stabName() {
        return consentService.getReadConsent("abank", "team062-1");
    }

    @PostMapping("/testPaymentConsent")
    public MakePaymentResponse getPaymentConsent(@RequestBody MakeSinglePaymentRequest request) {
        // return consentService.getPaymentConsent("abank", request);
        return paymentService.makeSinglePayment(request, "team062-1");
    }
}
    
