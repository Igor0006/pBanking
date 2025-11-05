package com.example.pbanking.dto;

import java.math.BigDecimal;
import java.util.List;

public record ProductConsentRequest(
    String requesting_bank,
    String client_id,
    boolean read_product_agreements,
    boolean open_product_agreements,
    boolean close_product_agreements,
    List<String> allowed_product_types,
    BigDecimal max_amount,
    String valid_until,
    String reason
) {
    
}
