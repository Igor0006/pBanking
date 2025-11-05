package com.example.pbanking.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.springframework.stereotype.Service;


import com.example.pbanking.exception.NotFoundException;
import com.example.pbanking.dto.BankClientLink;
import com.example.pbanking.dto.response.StatisticReposnse;
import com.example.pbanking.dto.response.TransactionsResponse;
import com.example.pbanking.dto.response.TransactionsResponse.TransactionStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DataService {
    private final UserService userService;
    private final PredictExpenseService predictService;
    private final TransactionService transactionService;
    private final AccountService accountService;

    public StatisticReposnse getStatistic() {
        Map<YearMonth, BigDecimal> map = getLastYearExpenses();
        if (map.isEmpty()) {
            throw new NotFoundException("No transactions found for the current users");
        }

        YearMonth latestMonth = map.keySet().stream()
                .reduce((first, second) -> second)
                .orElseThrow();
        int monthNum = latestMonth.getMonthValue();

        List<BigDecimal> features = new ArrayList<>();
        for (var entry : map.entrySet()) {
            features.add(entry.getValue());
        }
        if (!features.isEmpty()) {
            features.remove(features.size() - 1);
        }
        var predict = predictService.forecast(features.reversed().stream().map(BigDecimal::doubleValue).toList(), monthNum);
        return new StatisticReposnse(map, predict.currentAmount(), predict.nextAmount());
    }
    
    public Map<YearMonth, BigDecimal> getLastYearExpenses() {
        Map<YearMonth, BigDecimal> stats = new LinkedHashMap<>(13);

        LocalDateTime start = LocalDateTime.now(ZoneOffset.UTC)
                .withDayOfMonth(1)
                .withHour(0).withMinute(0).withSecond(0).withNano(0)
                .minusMonths(12);

        LocalDateTime cursor = start;
        for (int i = 0; i < 14; i++) {
            YearMonth ym = YearMonth.from(cursor);
            stats.put(ym, BigDecimal.ZERO);
            cursor = cursor.plusMonths(1);
        }

        Instant from = start.toInstant(ZoneOffset.UTC);
        Instant to = start.plusMonths(13).toInstant(ZoneOffset.UTC);

        forEachTransaction(from.toString(), to.toString(), transaction -> {
            BigDecimal issuedAmount = extractIssuedAmount(transaction);
            if (issuedAmount == null) {
                return;
            }

            var bookingDate = transaction.getBookingDateTime();
            if (bookingDate == null) {
                return;
            }

            YearMonth key = YearMonth.from(bookingDate);
            if (stats.containsKey(key)) {
                stats.merge(key, issuedAmount, BigDecimal::add);
            }
        });

        return stats;
    }
    
    public BigDecimal countGeneralExpens(String from_time, String to_time) {
        BigDecimal[] total = {BigDecimal.ZERO};
        forEachTransaction(from_time, to_time, transaction -> {
            BigDecimal issuedAmount = extractIssuedAmount(transaction);
            if (issuedAmount != null) {
                total[0] = total[0].add(issuedAmount);
            }
        });
        return total[0];
    }
     
    public BigDecimal countAccountExpens(String bank_id, String account_id, String from_time, String to_time) {
        Map<String, String> queryMap = Map.of("from_booking_date_time", from_time, "to_booking_date_time", to_time);
        BigDecimal total = BigDecimal.ZERO;

        TransactionsResponse response = transactionService.getTransactions(bank_id, account_id, queryMap);
        if (response == null) {
            return total;
        }

        for (var transaction : response.transactions()) {
            BigDecimal issuedAmount = extractIssuedAmount(transaction);
            if (issuedAmount != null) {
                total = total.add(issuedAmount);
            }
        }

        return total;
    } 

    private void forEachTransaction(String from, String to, Consumer<TransactionsResponse.Transaction> consumer) {
        Map<String, String> queryMap = Map.of("from_booking_date_time", from, "to_booking_date_time", to);
        List<BankClientLink> list = userService.getAllBankClientLinks();
        for (var pair : list) {
            for (var account : accountService.getAccounts(pair.bankId(), pair.clientId())) {
                TransactionsResponse response = transactionService.getTransactions(pair.bankId(), account.accountId(), queryMap);
                if (response == null) {
                    continue;
                }
                for (var transaction : response.transactions()) {
                    consumer.accept(transaction);
                }
            }
        }
    }

    private BigDecimal extractIssuedAmount(TransactionsResponse.Transaction transaction) {
        if (transaction == null || transaction.getStatus() != TransactionStatus.BOOKED) {
            return null;
        }
        var bankTransactionCode = transaction.getBankTransactionCode();
        if (bankTransactionCode == null) {
            return null;
        }

        String code = bankTransactionCode.getCode();
        if (code == null || !code.contains("Issued")) {
            return null;
        }

        var amount = transaction.getAmount();
        if (amount == null) {
            return null;
        }

        BigDecimal value = amount.getAmount();
        if (value == null) {
            return null;
        }
        return value;
    }   
}
