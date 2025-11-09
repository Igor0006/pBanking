package com.example.pbanking.dto.response;

import java.math.BigDecimal;

/**
 * API-facing view of products enriched with the bank identifier they belong to.
 */
public record BankProductResponse(
        String bankId,
        String productId,
        String productType,
        String productName,
        String description,
        BigDecimal interestRate,
        BigDecimal minAmount,
        BigDecimal maxAmount,
        Integer termMonths) {

    public static BankProductResponse from(String bankId, AvailableProductsResponse.Product product) {
        if (product == null) {
            return new BankProductResponse(bankId, null, null, null, null, null, null, null, null);
        }
        return new BankProductResponse(
                bankId,
                product.productId(),
                product.productType(),
                product.productName(),
                product.description(),
                product.interestRate(),
                product.minAmount(),
                product.maxAmount(),
                product.termMonths());
    }
}
