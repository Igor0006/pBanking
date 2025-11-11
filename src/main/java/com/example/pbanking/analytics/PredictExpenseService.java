package com.example.pbanking.analytics;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import ai.catboost.CatBoostError;
import ai.catboost.CatBoostModel;
import ai.catboost.CatBoostPredictions;

import com.example.pbanking.exception.BadRequestException;
import com.example.pbanking.exception.InternalServerException;

@Service
public class PredictExpenseService {
    private static final int MAX_LAG = 12;
    private final double alpha = 0.7;
    
    private final CatBoostModel curModel;
    private final CatBoostModel nextModel;

    public PredictExpenseService() {
        CatBoostModel cur;
        CatBoostModel next;
        try (InputStream is1 = new ClassPathResource("models/expense_cur.cbm").getInputStream();
             InputStream is2 = new ClassPathResource("models/expense_next.cbm").getInputStream()) {
            cur = CatBoostModel.loadModel(is1);
            next = CatBoostModel.loadModel(is2);
        } catch (IOException | CatBoostError e) {
            throw new InternalServerException("Failed to load expense prediction models", e);
        }
        this.curModel = cur;
        this.nextModel = next;
    }

    public double predictCurrent(float[] features) {
        try {
            CatBoostPredictions pred = curModel.predict(features, (String[]) null);
            return pred.get(0, 0);
        } catch (CatBoostError e) {
            throw new InternalServerException("Failed to predict current month expenses", e);
        }
    }

    public double predictNext(float[] features) {
        try {
            CatBoostPredictions pred = nextModel.predict(features, (String[]) null);
            return pred.get(0, 0);
        } catch (CatBoostError e) {
            throw new InternalServerException("Failed to predict next month expenses", e);
        }
    }

    private double computeHistoryMean(List<Double> history, double base) {
        double mean = history.stream()
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .filter(v -> v > 0.0)
                .average()
                .orElse(Double.NaN);

        if (Double.isNaN(mean)) {
            mean = (Double.isNaN(base) || base <= 0.0) ? 0.0 : base;
        }
        return mean;
    }

    public ExpenseForecast forecast(List<Double> history, int monthNum) {
        if (history == null || history.isEmpty()) {
            throw new BadRequestException("Expense history must not be empty");
        }
        System.out.println(history);
        float[] feats = buildFeatures(history, monthNum);
        double base = history.get(0);

        double ratioCur = predictCurrent(feats);
        double ratioNext = predictNext(feats);

        double predCurr = base * ratioCur;
        double predNext = predCurr * ratioNext;

        double histMean = computeHistoryMean(history, base);

        predCurr = alpha * predCurr + (1.0 - alpha) * histMean;
        predNext = alpha * predNext + (1.0 - alpha) * histMean;
        
        return new ExpenseForecast(predCurr, predNext, ratioCur, ratioNext);
    }

    public record ExpenseForecast(
            double currentAmount,
            double nextAmount,
            double ratioCurrent,
            double ratioNext) {
    }
    
    public static float[] buildFeatures(List<Double> history, int monthNum) {
        float[] feats = new float[14]; // 12 rlag + base + month

        // base = последний известный месяц (rlag_1). 0.0 трактуем как пропуск.
        Double baseObj = (history != null && !history.isEmpty()) ? history.get(0) : null;
        double baseVal = (baseObj == null) ? Double.NaN : baseObj.doubleValue();
        boolean baseMissing = Double.isNaN(baseVal) || baseVal == 0.0;

        // rlag_1..rlag_12
        for (int i = 0; i < MAX_LAG; i++) {
            float value;
            if (i < history.size()) {
                Double viObj = history.get(i);
                double vi = (viObj == null) ? Double.NaN : viObj.doubleValue();
                boolean viMissing = Double.isNaN(vi) || vi == 0.0;

                if (i == 0) {
                    // rlag_1: 1.0 если base есть, иначе NaN
                    value = baseMissing ? Float.NaN : 1.0f;
                } else {
                    // rlag_i: v/base, но если base или v отсутствуют (или равны 0.0), то NaN
                    value = (baseMissing || viMissing) ? Float.NaN : (float) (vi / baseVal);
                }
            } else {
                value = Float.NaN; // нет такого лага → пропуск
            }
            feats[i] = value; // индексы 0..11
        }

        // base_amount: сам масштаб. Если baseMissing — кладём NaN, CatBoost это понимает.
        feats[12] = baseMissing ? Float.NaN : (float) baseVal;

        // month_num (как есть)
        feats[13] = (float) monthNum;

        return feats;
    }
}
