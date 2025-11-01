package com.example.pbanking.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record TransactionsResponse(
        Data data,
        Meta meta) 
    {
    public List<Transaction> transactions() {
        return (data == null || data.transaction() == null) ? List.of() : data.transaction();
    }

    public record Data(
            List<Transaction> transaction) {
    }

    public record Links(
            String self,
            String next) {
    }

    public record Meta(
            Integer totalPages,
            Integer totalRecords,
            Integer currentPage,
            Integer pageSize) {
    }

    public record Transaction(
            String accountId,
            String transactionId,
            Amount amount,
            CreditDebitIndicator creditDebitIndicator,
            TransactionStatus status,
            OffsetDateTime bookingDateTime,
            OffsetDateTime valueDateTime,
            String transactionInformation,
            BankTransactionCode bankTransactionCode) {
    }

    public record Amount(
            BigDecimal amount,
            String currency) {
    }

    public record BankTransactionCode(
            String code) {
    }

    public enum CreditDebitIndicator {
        CREDIT, DEBIT;

        @JsonCreator
        public static CreditDebitIndicator from(String v) {
            if (v == null)
                return null;
            return CreditDebitIndicator.valueOf(v.trim().toUpperCase());
        }

        @JsonValue
        public String toValue() {
            // "Credit" / "Debit"
            String n = name().toLowerCase();
            return Character.toUpperCase(n.charAt(0)) + n.substring(1);
        }
    }

    public enum TransactionStatus {
        BOOKED, PENDING, REJECTED;

        @JsonCreator
        public static TransactionStatus from(String v) {
            if (v == null)
                return null;
            return TransactionStatus.valueOf(v.trim().toUpperCase());
        }

        @JsonValue
        public String toValue() {
            String n = name().toLowerCase();
            return Character.toUpperCase(n.charAt(0)) + n.substring(1);
        }
    }
}