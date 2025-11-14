package com.example.pbanking.transaction;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.pbanking.common.enums.PurposeType;
import com.example.pbanking.transaction.dto.response.TransactionsSummaryResponse.TransactionDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClassifierApiService {
    private static final String PREDICT_PATH = "/getPredict";
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(5);

    private final WebClient webClient;

    @Value("${classifier.service.url:http://localhost:8000}")
    private String classifierBaseUrl;

    public PurposeType predict(TransactionDto transaction, String userId) {
        if (transaction == null || transaction.getTransactionId() == null || userId == null || userId.isBlank()) {
            return PurposeType.NONE;
        }

        var uri = UriComponentsBuilder.fromUriString(classifierBaseUrl)
                .path(PREDICT_PATH)
                .queryParam("userId", userId)
                .build()
                .toUri();

        try {
            var response = webClient.post()
                    .uri(uri)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(transaction)
                    .retrieve()
                    .bodyToMono(ClassifierResponse.class)
                    .block(REQUEST_TIMEOUT);

            if (response == null || response.type == null) {
                return PurposeType.NONE;
            }
            return response.type;
        } catch (WebClientResponseException e) {
            log.warn("Classifier service error: status={} body={}", e.getStatusCode(), e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Failed to fetch classifier prediction", e);
        }
        return PurposeType.NONE;
    }

    private record ClassifierResponse(PurposeType type) { }
}
