package com.example.pbanking.dto.request;

import com.example.pbanking.model.enums.PurposeType;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UpdateTypeRequest(
        @JsonProperty(required = false) String bankId,
        String id,
        PurposeType type) {
}