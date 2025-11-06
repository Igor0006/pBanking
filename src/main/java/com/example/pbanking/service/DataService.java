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

import com.example.pbanking.dto.BankClientLink;
import com.example.pbanking.dto.response.StatisticReposnse;
import com.example.pbanking.dto.response.TransactionsResponse;
import com.example.pbanking.dto.response.TransactionsResponse.TransactionStatus;
import com.example.pbanking.exception.NotFoundException;
import com.example.pbanking.model.enums.PurposeType;
import com.example.pbanking.repository.AccountRepository;
import com.example.pbanking.repository.TransactionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DataService {
    private static final int MONTH_WINDOW = 13;

    private final UserService userService;
    private final PredictExpenseService predictService;
    private final TransactionService transactionService;
    private final AccountService accountService;
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    public StatisticReposnse getStatistic() {
        return getStatistic(null);
    }

    public StatisticReposnse getStatistic(PurposeType type) {
        PurposeType effectiveType = normalizeType(type);
        ExpenseWindow window = buildLastYearExpenses(effectiveType);
        if (effectiveType == null && !window.hasTransactions()) {
            throw new NotFoundException("No transactions found for the current users");
        }

        Map<YearMonth, BigDecimal> stats = window.stats();
        YearMonth latestMonth = stats.keySet().stream()
                .reduce((first, second) -> second)
                .orElseThrow();
        int monthNum = latestMonth.getMonthValue();

        double currentPredict = 0.0;
        double nextPredict = 0.0;
        if (effectiveType != PurposeType.BUSINESS) {
            List<BigDecimal> features = new ArrayList<>(stats.values());
            if (!features.isEmpty()) {
                features.remove(features.size() - 1);
            }
            var forecast = predictService.forecast(
                    features.reversed().stream().map(BigDecimal::doubleValue).toList(),
                    monthNum);
            currentPredict = forecast.currentAmount();
            nextPredict = forecast.nextAmount();
        }

        YearMonth nextMonth = latestMonth.plusMonths(1);
        stats.put(nextMonth, BigDecimal.ZERO);

        return new StatisticReposnse(stats, currentPredict, nextPredict);
    }

    public Map<YearMonth, BigDecimal> getLastYearExpenses() {
        return buildLastYearExpenses(null).stats();
    }

    public Map<YearMonth, BigDecimal> getLastYearExpenses(PurposeType type) {
        return buildLastYearExpenses(type).stats();
    }

    public BigDecimal countGeneralExpens(String from_time, String to_time) {
        BigDecimal[] total = { BigDecimal.ZERO };
        forEachTransaction(from_time, to_time, null, transaction -> {
            BigDecimal expense = extractExpenseAmount(transaction);
            if (expense != null) {
                total[0] = total[0].add(expense);
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
            BigDecimal expense = extractExpenseAmount(transaction);
            if (expense != null) {
                total = total.add(expense);
            }
        }

        return total;
    }

    private ExpenseWindow buildLastYearExpenses(PurposeType type) {
        PurposeType effectiveType = normalizeType(type);
        Map<YearMonth, BigDecimal> stats = new LinkedHashMap<>(MONTH_WINDOW);

        LocalDateTime start = LocalDateTime.now(ZoneOffset.UTC)
                .withDayOfMonth(1)
                .withHour(0).withMinute(0).withSecond(0).withNano(0)
                .minusMonths(MONTH_WINDOW - 1L);

        LocalDateTime cursor = start;
        for (int i = 0; i < MONTH_WINDOW; i++) {
            stats.put(YearMonth.from(cursor), BigDecimal.ZERO);
            cursor = cursor.plusMonths(1);
        }

        Instant from = start.toInstant(ZoneOffset.UTC);
        Instant to = cursor.toInstant(ZoneOffset.UTC);

        boolean[] hasTransactions = { false };
        forEachTransaction(from.toString(), to.toString(), effectiveType, transaction -> {
            BigDecimal expense = extractExpenseAmount(transaction);
            if (expense == null) {
                return;
            }

            var bookingDate = transaction.getBookingDateTime();
            if (bookingDate == null) {
                return;
            }

            YearMonth key = YearMonth.from(bookingDate);
            if (stats.containsKey(key)) {
                stats.merge(key, expense, BigDecimal::add);
                hasTransactions[0] = true;
            }
        });

        return new ExpenseWindow(stats, hasTransactions[0]);
    }

    private void forEachTransaction(String from, String to, PurposeType typeFilter,
            Consumer<TransactionsResponse.Transaction> consumer) {
        Map<String, String> queryMap = Map.of("from_booking_date_time", from, "to_booking_date_time", to);
        List<BankClientLink> links = userService.getAllBankClientLinks();
        for (var link : links) {
            String bankId = link.bankId();
            for (var account : accountService.getAccounts(bankId, link.clientId())) {
                String accountId = account.getAccountId();
                TransactionsResponse response = transactionService.getTransactions(bankId, accountId, queryMap);
                if (response == null) {
                    continue;
                }
                for (var transaction : response.transactions()) {
                    if (!matchesType(typeFilter, bankId, accountId, transaction)) {
                        continue;
                    }
                    consumer.accept(transaction);
                }
            }
        }
    }

    private boolean matchesType(PurposeType filter, String bankId, String accountId,
            TransactionsResponse.Transaction transaction) {
        if (filter == null) {
            return true;
        }
        PurposeType resolved = resolveTransactionPurpose(bankId, accountId, transaction);
        return resolved == filter;
    }

    private PurposeType resolveTransactionPurpose(String bankId, String accountId,
            TransactionsResponse.Transaction transaction) {
        if (transaction == null) {
            return PurposeType.NONE;
        }

        String transactionId = transaction.getTransactionId();
        if (transactionId != null && !transactionId.isBlank()) {
            var storedType = transactionRepository.findTypeByTransactionId(transactionId);
            if (storedType.isPresent() && storedType.get() != null && storedType.get() != PurposeType.NONE) {
                return storedType.get();
            }
        }

        if (accountId != null && !accountId.isBlank()) {
            var accountType = accountRepository.findByAccountIdAndBankId(accountId, bankId)
                    .map(acc -> acc.getType())
                    .orElse(null);
            if (accountType != null && accountType != PurposeType.NONE) {
                return accountType;
            }
        }

        PurposeType inlineType = transaction.getType();
        return inlineType == null ? PurposeType.NONE : inlineType;
    }

    private BigDecimal extractExpenseAmount(TransactionsResponse.Transaction transaction) {
        if (transaction == null || transaction.getStatus() != TransactionStatus.BOOKED) {
            return null;
        }

        var amount = transaction.getAmount();
        if (amount == null || amount.getAmount() == null) {
            return null;
        }

        var bankTransactionCode = transaction.getBankTransactionCode();
        String code = bankTransactionCode == null ? null : bankTransactionCode.getCode();
        var indicator = transaction.getCreditDebitIndicator();

        boolean isDebit = indicator == TransactionsResponse.CreditDebitIndicator.DEBIT;

        if (isDebit) {
            if (code == null || !code.contains("Issued")) {
                return null;
            }
            return amount.getAmount();
        }

        return null;
    }

    private PurposeType normalizeType(PurposeType type) {
        if (type == null || type == PurposeType.NONE) {
            return null;
        }
        return type;
    }

    private record ExpenseWindow(Map<YearMonth, BigDecimal> stats, boolean hasTransactions) {
    }
}
