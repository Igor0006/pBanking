package com.example.pbanking.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "secret")
public class TPPConfig {
    private String teamCode;
    private String teamSecret;

    public String getRequestinBankId() {
        return teamCode;
    }
    
    public String getRequestinBankName() {
        return teamSecret;
    }

    public void setTeamCode(String teamCode) {
        this.teamCode = teamCode;
    }

    public void setTeamSecret(String teamSecret) {
        this.teamSecret = teamSecret;
    }
}
