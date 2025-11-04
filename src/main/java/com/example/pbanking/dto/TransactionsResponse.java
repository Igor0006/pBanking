package com.example.pbanking.dto;

import com.example.pbanking.model.enums.TransactionType;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionsResponse {
    private TxData data;
    private Meta meta;

    public List<Transaction> transactions() {
        return (data == null || data.getTransaction() == null) ? List.of() : data.getTransaction();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TxData {
        private List<Transaction> transaction;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Links {
        private String self;
        private String next;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Meta {
        private Integer totalPages;
        private Integer totalRecords;
        private Integer currentPage;
        private Integer pageSize;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Transaction {
        private String accountId;
        private String transactionId;

        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        private TransactionType type = TransactionType.NONE;
        private Amount amount;
        private CreditDebitIndicator creditDebitIndicator;
        private TransactionStatus status;
        private OffsetDateTime bookingDateTime;
        private OffsetDateTime valueDateTime;
        private String transactionInformation;
        private BankTransactionCode bankTransactionCode;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Amount {
        private BigDecimal amount;
        private String currency;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BankTransactionCode {
        private String code;
    }

    public enum CreditDebitIndicator {
        CREDIT, DEBIT;

        @JsonCreator
        public static CreditDebitIndicator from(String v) {
            if (v == null) return null;
            return CreditDebitIndicator.valueOf(v.trim().toUpperCase());
        }

        @JsonValue
        public String toValue() {
            String n = name().toLowerCase();
            return Character.toUpperCase(n.charAt(0)) + n.substring(1);
        }
    }

    public enum TransactionStatus {
        BOOKED, PENDING, REJECTED;

        @JsonCreator
        public static TransactionStatus from(String v) {
            if (v == null) return null;
            return TransactionStatus.valueOf(v.trim().toUpperCase());
        }

        @JsonValue
        public String toValue() {
            String n = name().toLowerCase();
            return Character.toUpperCase(n.charAt(0)) + n.substring(1);
        }
    }
}