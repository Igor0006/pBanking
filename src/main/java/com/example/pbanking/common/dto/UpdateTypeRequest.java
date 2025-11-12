package com.example.pbanking.common.dto;

import com.example.pbanking.common.enums.PurposeType;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UpdateTypeRequest(
        @JsonProperty(required = false) String bankId,
        String id,
        PurposeType type) {
}