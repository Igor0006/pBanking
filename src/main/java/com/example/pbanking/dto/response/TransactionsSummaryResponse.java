package com.example.pbanking.dto.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.example.pbanking.dto.response.TransactionsResponse.CreditDebitIndicator;
import com.example.pbanking.dto.response.TransactionsResponse.TransactionStatus;
import com.example.pbanking.model.enums.PurposeType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionsSummaryResponse {
    private List<TransactionDto> transactions;
    private Meta meta;

    public static TransactionsSummaryResponse from(TransactionsResponse source) {
        if (source == null) {
            return new TransactionsSummaryResponse(Collections.emptyList(), null);
        }

        List<TransactionDto> transactions = source.transactions().stream()
                .filter(Objects::nonNull)
                .map(TransactionDto::from)
                .collect(Collectors.toList());

        return new TransactionsSummaryResponse(transactions, Meta.from(source.getMeta()));
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransactionDto {
        private String accountId;
        private String transactionId;
        private PurposeType type = PurposeType.NONE;
        private Amount amount;
        private CreditDebitIndicator creditDebitIndicator;
        private TransactionStatus status;
        private OffsetDateTime bookingDateTime;
        private OffsetDateTime valueDateTime;
        private String transactionInformation;
        private String code;
        private Card card;

        public static TransactionDto from(TransactionsResponse.Transaction transaction) {
            if (transaction == null) {
                return null;
            }

            TransactionDto dto = new TransactionDto();
            dto.setAccountId(transaction.getAccountId());
            dto.setTransactionId(transaction.getTransactionId());
            dto.setType(transaction.getType());
            dto.setAmount(Amount.from(transaction.getAmount()));
            dto.setCreditDebitIndicator(transaction.getCreditDebitIndicator());
            dto.setStatus(transaction.getStatus());
            dto.setBookingDateTime(transaction.getBookingDateTime());
            dto.setValueDateTime(transaction.getValueDateTime());
            dto.setTransactionInformation(transaction.getTransactionInformation());
            dto.setCode(mapTransactionCode(transaction.getBankTransactionCode()));
            dto.setCard(Card.from(transaction.getCard()));
            return dto;
        }

        private static String mapTransactionCode(TransactionsResponse.BankTransactionCode source) {
            if (source == null || source.getCode() == null) {
                return null;
            }
            String raw = source.getCode().trim();
            if (raw.isEmpty()) {
                return null;
            }
            if ("01".equals(raw) || "issued".equalsIgnoreCase(raw)) {
                return "issued";
            }
            if ("02".equals(raw) || "received".equalsIgnoreCase(raw)) {
                return "received";
            }
            return raw;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Amount {
        private BigDecimal amount;
        private String currency;

        public static Amount from(TransactionsResponse.Amount source) {
            if (source == null) {
                return null;
            }
            return new Amount(source.getAmount(), source.getCurrency());
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Card {
        private String cardId;
        private String cardNumber;
        private String cardType;
        private String cardName;

        public static Card from(TransactionsResponse.Card source) {
            if (source == null) {
                return null;
            }
            return new Card(source.getCardId(), source.getCardNumber(), source.getCardType(), source.getCardName());
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Meta {
        private Integer totalPages;
        private Integer totalRecords;
        private Integer currentPage;
        private Integer pageSize;

        public static Meta from(TransactionsResponse.Meta source) {
            if (source == null) {
                return null;
            }
            return new Meta(source.getTotalPages(), source.getTotalRecords(), source.getCurrentPage(), source.getPageSize());
        }
    }
}
