package com.example.pbanking.transaction.dto.response;

import com.example.pbanking.common.enums.PurposeType;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.JsonNode;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionsResponse {
    private TxData data;
    private Links links;
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
        private String bankId;
        private String accountId;
        private String transactionId;

        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        private PurposeType type = PurposeType.NONE;
        private Amount amount;
        private CreditDebitIndicator creditDebitIndicator;
        private TransactionStatus status;
        private OffsetDateTime bookingDateTime;
        private OffsetDateTime valueDateTime;
        private String transactionInformation;
        private BankTransactionCode bankTransactionCode;
        private Merchant merchant;
        private TransactionLocation transactionLocation;
        private Card card;
        private Counterparty counterparty;
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

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Merchant {
        private String merchantId;
        private String name;
        private String mccCode;
        private String category;
        private String city;
        private String country;
        private String address;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransactionLocation {
        private String city;
        private String country;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Card {
        private String cardId;
        private String cardNumber;
        private String cardType;
        private String cardName;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Counterparty {
        private String counterpartyId;
        private String name;
        private String accountId;
        private String bankId;

        @JsonCreator
        public static Counterparty fromJson(JsonNode node) {
            if (node == null || node.isNull()) {
                return new Counterparty();
            }
            if (node.isTextual()) {
                return new Counterparty(node.asText(), null, null, null);
            }
            Counterparty cp = new Counterparty();
            cp.setCounterpartyId(text(node.get("counterpartyId")));
            cp.setName(text(node.get("name")));
            cp.setAccountId(text(node.get("accountId")));
            cp.setBankId(text(node.get("bankId")));
            return cp;
        }

        private static String text(JsonNode node) {
            return (node == null || node.isNull()) ? null : node.asText();
        }
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
        BOOKED, COMPLETED, PENDING, REJECTED;

        @JsonCreator
        public static TransactionStatus from(String v) {
            if (v == null) return null;
            return TransactionStatus.valueOf(v.trim().toUpperCase());
        }

        public boolean isSettled() {
            return this == BOOKED || this == COMPLETED;
        }

        @JsonValue
        public String toValue() {
            String n = name().toLowerCase();
            return Character.toUpperCase(n.charAt(0)) + n.substring(1);
        }
    }
}
