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

import com.example.pbanking.config.TPPConfig;
import com.example.pbanking.dto.AccountSummary;
import com.example.pbanking.dto.AccountsResponse;
import com.example.pbanking.dto.AvailableProductsResponse;
import com.example.pbanking.dto.StatisticReposnse;
import com.example.pbanking.dto.TransactionsResponse;
import com.example.pbanking.dto.AvailableProductsResponse.Product;
import com.example.pbanking.model.enums.ConsentType;
import com.example.pbanking.repository.CredentialsRepository.BankClientPair;
import com.example.pbanking.dto.TransactionsResponse.TransactionStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DataRecieveService {
    private final WebClientExecutor wc;
    private final BankTokenService tokenService;
    private final ConsentService consentService;
    private final UserService userService;
    private final PredictExpenseService predictService;
    private final TPPConfig props;
    private final static String ACCOUNTS_PATH = "/accounts";
    private final static String TRANSACTIONS_PATH = "/transactions";
    private final static String AVAILABLE_PRODUCTS_PATH = "/products";
    
    public List<AccountSummary> getAccounts(String bank_id, String client_id) {
        var headersMap = Map.of("x-requesting-bank", props.getRequestingBankId(), "x-consent-id",  consentService.getConsentForBank(bank_id, ConsentType.READ));
        var queryMap = Map.of("client_id", client_id);
        var response = wc.get(bank_id, ACCOUNTS_PATH, queryMap, headersMap, tokenService.getBankToken(bank_id), AccountsResponse.class);
        return response.accounts().stream()
                .map(account -> new AccountSummary(
                        account.accountId(),
                        account.status(),
                        account.currency(),
                        account.accountSubType(),
                        account.nickname(),
                        account.openingDate(),
                        account.accountReferences(),
                        getAccountBalance(account.accountId(), bank_id)))
                .toList();
    }
    
    public BigDecimal getAccountBalance(String account_id, String bank_id){
        var headersMap = Map.of("x-requesting-bank", props.getRequestingBankId(), "x-consent-id", consentService.getConsentForBank(bank_id, ConsentType.READ));
        String path = ACCOUNTS_PATH + "/" + account_id + "/balances";
        var response = wc.get(bank_id, path, null, headersMap, tokenService.getBankToken(bank_id), BalanceResponse.class);
        if (response == null || response.data() == null || response.data().balance() == null || response.data().balance().isEmpty()) {
            return BigDecimal.ZERO;
        }
        BalanceResponse.Balance balance = response.data().balance().get(0);
        if (balance.amount() == null || balance.amount().amount() == null) {
            return BigDecimal.ZERO;
        }
        return balance.amount().amount();
    } 
    
    public TransactionsResponse getTransactions(String bank_id, String account_id, Map<String, String> queryMap) {
        var headersMap = Map.of("x-consent-id", consentService.getConsentForBank(bank_id, ConsentType.READ), "x-requesting-bank", props.getRequestingBankId());
        String path = ACCOUNTS_PATH + "/" + account_id + TRANSACTIONS_PATH;
        return wc.get(bank_id, path, queryMap, headersMap, tokenService.getBankToken(bank_id),
                TransactionsResponse.class);
    }
    
    public StatisticReposnse getStatistic() {
        Map<YearMonth, BigDecimal> map = getLastYearExpenses();
        if (map.isEmpty()) {
            throw new NullPointerException("No transactions in users accounts");
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
        for (int i = 0; i < 13; i++) {
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

            var bookingDate = transaction.bookingDateTime();
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

        TransactionsResponse response = getTransactions(bank_id, account_id, queryMap);
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
    
    public List<Product> getAvailableProducts(String bank_id) {
        return wc.get(bank_id, AVAILABLE_PRODUCTS_PATH, null, null, tokenService.getBankToken(bank_id), AvailableProductsResponse.class).products();
    }

    private void forEachTransaction(String from, String to, Consumer<TransactionsResponse.Transaction> consumer) {
        Map<String, String> queryMap = Map.of("from_booking_date_time", from, "to_booking_date_time", to);
        List<BankClientPair> list = userService.getUserClientIds();
        for (var pair : list) {
            for (var account : getAccounts(pair.getBankId(), pair.getClientId())) {
                TransactionsResponse response = getTransactions(pair.getBankId(), account.accountId(), queryMap);
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
        if (transaction == null || transaction.status() != TransactionStatus.BOOKED) {
            return null;
        }

        var bankTransactionCode = transaction.bankTransactionCode();
        if (bankTransactionCode == null) {
            return null;
        }

        String code = bankTransactionCode.code();
        if (code == null || !code.contains("Issued")) {
            return null;
        }

        var amount = transaction.amount();
        if (amount == null) {
            return null;
        }

        BigDecimal value = amount.amount();
        if (value == null) {
            return null;
        }

        return value;
    }
    
    public record BalanceResponse(Data data) {
        public record Data(List<Balance> balance) {
        }
        public record Balance(
                String accountId,
                String type,
                String dateTime,
                Amount amount,
                String creditDebitIndicator) {
        }
        public record Amount(
                BigDecimal amount,
                String currency) {
        }
    }
}
