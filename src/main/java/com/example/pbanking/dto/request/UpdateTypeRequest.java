package com.example.pbanking.dto.request;

import com.example.pbanking.model.enums.TransactionType;

public record UpdateTypeRequest(String transaction_id, TransactionType type) { }