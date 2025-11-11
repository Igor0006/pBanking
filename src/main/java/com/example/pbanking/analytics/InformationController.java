package com.example.pbanking.analytics;

import java.math.BigDecimal;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.pbanking.analytics.dto.StatisticReposnse;
import com.example.pbanking.common.enums.PurposeType;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/data")
public class InformationController {
    private final DataService dataService;
    
    @GetMapping("/expens")
    public ResponseEntity<BigDecimal> getExpens(@RequestParam String from_booking_date_time, @RequestParam String to_booking_date_time, 
                                @RequestParam(required = false) String bank_id, @RequestParam(required = false) String account_id) {
        if (bank_id == null && account_id == null)
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(dataService.countGeneralExpens(from_booking_date_time, to_booking_date_time));
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(dataService.countAccountExpens(bank_id, account_id, from_booking_date_time, to_booking_date_time));
    }
    
    @GetMapping("/statistic")
    public ResponseEntity<StatisticReposnse> getStats(@RequestParam(required = false) PurposeType type) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(dataService.getStatistic(type));
    }    
}
