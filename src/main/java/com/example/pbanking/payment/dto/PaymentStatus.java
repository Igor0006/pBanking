package com.example.pbanking.payment.dto;

public record PaymentStatus(
    Data data,
    Object links,
    Object meta
) {
    public record Data (
        String paymentId,
        String status,
        String creationDateTime,
        String statusUpdateTime
    ) {}

}
