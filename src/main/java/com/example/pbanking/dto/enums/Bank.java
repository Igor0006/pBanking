package com.example.pbanking.dto.enums;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Bank {
    VBANK("VBank", "Virtual Bank"),
    ABANK("ABank", "Awesome Bank"),
    SBANK("SBank", "Smart Bank");

    private String code;
    private String name;

    Bank(String code, String name) {
        this.code = code;
        this.name = name;
    }

    @JsonValue
    public String getCode() {
        return this.code;
    }

    public String getName() {
        return this.name;
    }

    // TODO: Add HttpMessageNotReadableException handling
    @JsonCreator
    public Bank fromCode(String code) {
        return Arrays.stream(values())
                .filter(bank -> bank.code.equals(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown bank: " + code));
    }
}
