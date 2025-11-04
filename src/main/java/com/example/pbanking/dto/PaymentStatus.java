package com.example.pbanking.dto;

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
