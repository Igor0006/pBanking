package com.example.pbanking.user.dto.response;

import java.time.Instant;
import java.util.List;

import com.example.pbanking.user.dto.BankClientLink;
import com.example.pbanking.common.enums.UserStatus;

public record UserInformation(List<BankClientLink> bankClientLinks, UserStatus status, Instant premiumExpireDate, String name, String surname, String username) {
}
