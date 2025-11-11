package com.example.pbanking.consent.dto.request;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;

import lombok.Data;

@Data
public class AccountConsentRequestBody {
    String client_id;
    List<String> permissions = Arrays.asList("ReadAccountsDetail",
            "ReadBalances",
            "ReadTransactionsDetail");
    String reason = "Accounts agregation";
    @Value("${bank.id}")
    String requesting_bank;
    @Value("${bank.name}")
    String requesting_bank_name;
}
