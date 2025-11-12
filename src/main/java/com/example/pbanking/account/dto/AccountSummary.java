package com.example.pbanking.account.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.example.pbanking.account.dto.response.AccountsResponse;
import com.example.pbanking.common.enums.PurposeType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AccountSummary {

    private String accountId;
    private String bankId;
    private String status;
    private String currency;
    private String accountSubType;
    private String nickname;
    private LocalDate openingDate;

    @JsonProperty("account")
    private List<AccountsResponse.AccountReference> account;

    private BigDecimal amount;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private PurposeType purposeType = PurposeType.NONE;
    
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String description = null;

    public AccountSummary(String accountId, String bankId, String status, String currency, String accountSubType,
                          String nickname, LocalDate openingDate,
                          List<AccountsResponse.AccountReference> account, BigDecimal amount) {
        this.accountId = accountId;
        this.bankId = bankId;
        this.status = status;
        this.currency = currency;
        this.accountSubType = accountSubType;
        this.nickname = nickname;
        this.openingDate = openingDate;
        this.account = account;
        this.amount = amount;
    }
}
