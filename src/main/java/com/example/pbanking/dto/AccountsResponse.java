package com.example.pbanking.dto;

import lombok.Data;
import java.util.List;

@Data
public class AccountsResponse {
    private AccountsData data;
    private AccountsLinks links;
    private AccountsMeta meta;

    @Data
    public static class AccountsData {
        private List<Account> account;
    }

    @Data
    public static class Account {
        private String accountId;
        private String status;
        private String currency;
        private String accountType;
        private String accountSubType;
        private String nickname;
        private String openingDate;
        private List<AccountIdentification> account;
    }

    @Data
    public static class AccountIdentification {
        private String schemeName;
        private String identification;
        private String name;
    }

    @Data
    public static class AccountsLinks {
        private String self;
    }

    @Data
    public static class AccountsMeta {
        private int totalPages;
    }
}