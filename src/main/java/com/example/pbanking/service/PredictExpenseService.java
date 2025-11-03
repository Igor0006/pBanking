package com.example.pbanking.service;

import java.io.InputStream;
import java.util.List;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import ai.catboost.CatBoostModel;
import ai.catboost.CatBoostPredictions;

@Service
public class PredictExpenseService {
    private static final int MAX_LAG = 12;
    
    private final CatBoostModel curModel;
    private final CatBoostModel nextModel;

    public PredictExpenseService() throws Exception {
        try (InputStream is1 = new ClassPathResource("models/expense_cur.cbm").getInputStream();
             InputStream is2 = new ClassPathResource("models/expense_next.cbm").getInputStream()) {
            this.curModel = CatBoostModel.loadModel(is1);
            this.nextModel = CatBoostModel.loadModel(is2);
        }
    }

    public double predictCurrent(float[] features) throws Exception {
        CatBoostPredictions pred = curModel.predict(features, (String[]) null);
        return pred.get(0, 0);
    }

    public double predictNext(float[] features) throws Exception {
        CatBoostPredictions pred = nextModel.predict(features, (String[]) null);
        return pred.get(0, 0);
    }

    public ExpenseForecast forecast(List<Double> history, int monthNum) throws Exception {
        float[] feats = buildFeatures(history, monthNum);
        double base = history.get(0);

        double ratioCur = predictCurrent(feats);
        double curAmount = base * ratioCur;

        double ratioNext = predictNext(feats);
        double nextAmount = curAmount * ratioNext;

        return new ExpenseForecast(curAmount, nextAmount, ratioCur, ratioNext);
    }

    public record ExpenseForecast(
            double currentAmount,
            double nextAmount,
            double ratioCurrent,
            double ratioNext) {
    }
    
    public static float[] buildFeatures(List<Double> history, int monthNum) {
        float[] feats = new float[14]; // 12 rlag + base + month
        double base = history.get(0); // это наш rlag_1

        // rlag_i
        for (int i = 0; i < MAX_LAG; i++) {
            float value;
            if (i < history.size()) {
                double v = history.get(i);
                if (i == 0) {
                    value = 1.0f; // rlag_1 всегда 1
                } else {
                    value = (base != 0.0) ? (float) (v / base) : Float.NaN;
                }
            } else {
                value = Float.NaN;
            }
            feats[i] = value; // 0..11
        }

        // base_amount
        feats[12] = (float) base;

        // month_num
        feats[13] = (float) monthNum;

        return feats;
    }
}
