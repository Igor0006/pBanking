package com.example.pbanking.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import com.example.pbanking.model.BankEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@ConfigurationProperties(prefix = "banks")
public class BanksProperties {
    private List<BankEntry> list = new ArrayList<>();

    public List<BankEntry> getList() {
        return list;
    }

    public void setList(List<BankEntry> list) {
        this.list = list == null ? new ArrayList<>() : list;
    }
    
    public Map<String, String> getUrlMap() {
        return list.stream()
                .collect(Collectors.toMap(BankEntry::id, BankEntry::url));
    }
}
