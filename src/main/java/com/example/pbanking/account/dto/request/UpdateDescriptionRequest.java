package com.example.pbanking.account.dto.request;

public record UpdateDescriptionRequest(String bankId, String id, String text) {
}
