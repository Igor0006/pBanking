package com.example.pbanking.consent.dto.request;

import java.math.BigDecimal;
import java.util.List;

public record ProductConsentApiRequest(
    String bank_id,
    boolean read_product_agreements,
    boolean open_product_agreements,
    boolean close_product_agreements,
    List<String> allowed_product_types,
    BigDecimal max_amount, //макс сумма для открытия одного продукта
    String valid_until
) {
    
}
