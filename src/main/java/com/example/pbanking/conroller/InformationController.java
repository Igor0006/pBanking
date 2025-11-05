package com.example.pbanking.conroller;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.pbanking.service.DataService;
import com.example.pbanking.service.TransactionService;

import lombok.RequiredArgsConstructor;

import com.example.pbanking.dto.response.StatisticReposnse;
import com.example.pbanking.dto.response.TransactionsResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/data")
public class InformationController {
    private final DataService dataService;
    private final TransactionService transactionService;
    
    @GetMapping("/transactions/{bank_id}/{account_id}")
    public ResponseEntity<TransactionsResponse> getMethodName(@PathVariable String bank_id, @PathVariable String account_id, @RequestParam(required = false) String from_booking_date_time,
                                @RequestParam(required = false) String to_booking_date_time, @RequestParam(defaultValue = "1") String page, 
                                @RequestParam(defaultValue = "50") String limit, Authentication auth) {
        
        Map<String, String> queryMap = Map.of("page", page, "limit", limit);
        if (from_booking_date_time != null) queryMap.put("from_booking_date_time", from_booking_date_time);
        if (to_booking_date_time != null) queryMap.put("to_booking_date_time", to_booking_date_time);
        var roles = AuthorityUtils.authorityListToSet(auth.getAuthorities());
        if (roles.contains("ROLE_PREMIUM")) {
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(
                    transactionService.getTransactionsPrime(bank_id, account_id, queryMap));
        }
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(
                transactionService.getTransactions(bank_id, account_id, queryMap));
    }   
    
    @GetMapping("/expens")
    public ResponseEntity<BigDecimal> getExpens(@RequestParam String from_booking_date_time, @RequestParam String to_booking_date_time, 
                                @RequestParam(required = false) String bank_id, @RequestParam(required = false) String account_id) {
        if (bank_id == null && account_id == null)
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(dataService.countGeneralExpens(from_booking_date_time, to_booking_date_time));
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(dataService.countAccountExpens(bank_id, account_id, from_booking_date_time, to_booking_date_time));
    }
    
    @GetMapping("/statistic")
    public ResponseEntity<StatisticReposnse> getStats() {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(dataService.getStatistic());
    }
    
}
