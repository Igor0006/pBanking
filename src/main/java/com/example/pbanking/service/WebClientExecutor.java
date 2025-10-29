package com.example.pbanking.service;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class WebClientExecutor {
    private final WebClient webClient;

    public <T> T get(String url, String token, Class<T> responseType) {
        try {
            return webClient.get()
                    .uri(url)
                    .headers(h -> applyAuthorization(h, token))
                    .retrieve()
                    .bodyToMono(responseType)
                    .block(); // блокируем (обычный вызов)
        } catch (WebClientResponseException e) {
            System.err.println("Ошибка запроса: " + e.getStatusCode() + " " + e.getResponseBodyAsString());
            throw e;
        }
    }

    public <B, T> T post(String url, B body, String token, Class<T> responseType) {
        try {
            return webClient.post()
                    .uri(url)
                    .headers(h -> applyAuthorization(h, token))
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(responseType)
                    .block();
        } catch (WebClientResponseException e) {
            System.err.println("Ошибка запроса: " + e.getStatusCode() + " " + e.getResponseBodyAsString());
            throw e;
        }
    }

    private void applyAuthorization(HttpHeaders headers, String token) {
        if (token == null) {
            return;
        }

        String sanitized = token.strip();
        if (sanitized.isEmpty()) {
            return;
        }

        if (sanitized.length() >= 7 && sanitized.regionMatches(true, 0, "Bearer ", 0, 7)) {
            headers.set(HttpHeaders.AUTHORIZATION, sanitized);
        } else {
            headers.setBearerAuth(sanitized);
        }
    }
}
