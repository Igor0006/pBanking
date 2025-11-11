package com.example.pbanking.transaction;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.pbanking.common.dto.UpdateTypeRequest;
import com.example.pbanking.transaction.dto.response.TransactionsResponse;
import com.example.pbanking.transaction.dto.response.TransactionsSummaryResponse;
import com.example.pbanking.transaction.TransactionService;

import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequiredArgsConstructor
@RequestMapping("api/transaction")
public class TransactionController {
    private final TransactionService transactionService;
    
    @GetMapping("/getTransactions/{bank_id}/{account_id}")
    public ResponseEntity<TransactionsSummaryResponse> getMethodName(@PathVariable String bank_id, @PathVariable String account_id, @RequestParam(required = false) String from_booking_date_time,
                                @RequestParam(required = false) String to_booking_date_time, @RequestParam(defaultValue = "1") String page, 
                                @RequestParam(defaultValue = "50") String limit, @RequestParam(defaultValue = "false") String predictParam, Authentication auth) {
        
        Map<String, String> queryMap = new HashMap<>();
        queryMap.put("page", page);
        queryMap.put("limit", limit);
        if (from_booking_date_time != null) queryMap.put("from_booking_date_time", from_booking_date_time);
        if (to_booking_date_time != null) queryMap.put("to_booking_date_time", to_booking_date_time);
        boolean predict = Boolean.parseBoolean(predictParam);
        var roles = AuthorityUtils.authorityListToSet(auth.getAuthorities());
        TransactionsResponse response;
        if (roles.contains("ROLE_PREMIUM")) {
            response = transactionService.getTransactionsPrime(bank_id, account_id, queryMap, predict);
        } else {
            response = transactionService.getTransactions(bank_id, account_id, queryMap);
        }
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(TransactionsSummaryResponse.from(response));
    }   
    
    @PostMapping("/setType")
    @Secured("ROLE_PREMIUM")
    public ResponseEntity<Void> postMethodName(@RequestBody UpdateTypeRequest request) {
        transactionService.setTypeForTransaction(request.id(), request.type());
        return ResponseEntity.status(200).body(null);    
    }
    
}
