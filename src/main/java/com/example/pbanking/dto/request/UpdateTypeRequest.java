package com.example.pbanking.dto.request;

import com.example.pbanking.model.enums.PurposeType;

public record UpdateTypeRequest(String id, PurposeType type) { }