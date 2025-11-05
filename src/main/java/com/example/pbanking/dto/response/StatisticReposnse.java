package com.example.pbanking.dto.response;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Map;

public record StatisticReposnse(Map<YearMonth, BigDecimal> statistic, Double currentPredict, Double nextPredict) {
    
}
