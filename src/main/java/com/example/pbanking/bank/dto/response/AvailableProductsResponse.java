package com.example.pbanking.bank.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AvailableProductsResponse(Data data) {

    public List<Product> products() {
        return data != null && data.product() != null
                ? data.product()
                : Collections.emptyList();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Data(List<Product> product) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Product(
            String productId,
            String productType,
            String productName,
            String description,
            BigDecimal interestRate,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            Integer termMonths) {
    }
}