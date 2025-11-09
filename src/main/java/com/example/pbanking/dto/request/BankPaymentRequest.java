package com.example.pbanking.dto.request;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class BankPaymentRequest {

    public BankPaymentRequest(BigDecimal amount, String currency, String debtorAccount, String creditorAccount, String debtorScheme, String creditorScheme, String comment) {
        this.data = new DataClass(new Initiation(new Amount(amount.toString(), currency), new Account(debtorScheme, debtorAccount),
                new CreditorAccount(creditorScheme, creditorAccount), comment));
    }

    private DataClass data;

    public void setCreditorBank(String bankCode) {
        this.data.initiation.creditorAccount.bank_code = bankCode;
    }

    @AllArgsConstructor
    @Data
    public class DataClass {
        private Initiation initiation;
    }

    @AllArgsConstructor
    @Data
    public class Amount {
        private String amount;
        private String currency;
    }

    @AllArgsConstructor
    @Data
    public class Account {
        private String schemeName;
        private String identification;
    }

    @AllArgsConstructor
    @Data
    public class CreditorAccount {
        public CreditorAccount(String scheme, String identification) {
            this.schemeName = scheme;
            this.identification = identification;
        }
        private String schemeName;
        private String identification;
        String bank_code = null;
    }

    @AllArgsConstructor
    @Data
    public class Initiation {
        private Amount instructedAmount;
        private Account debtorAccount;
        private CreditorAccount creditorAccount;
        private String comment;
    }
}
