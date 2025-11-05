package com.example.pbanking.dto.response;

import java.util.List;

import com.example.pbanking.dto.BankClientLink;
import com.example.pbanking.model.enums.UserStatus;

public record UserInformation(List<BankClientLink> bankClientLinks, UserStatus status) {
    
}
