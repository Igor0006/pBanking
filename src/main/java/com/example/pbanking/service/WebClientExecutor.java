package com.example.pbanking.service;

import java.net.URI;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.example.pbanking.config.BanksProperties;
import com.example.pbanking.utils.UrlBuilder;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class WebClientExecutor {
    private final WebClient webClient;
    private final BanksProperties banks;

    public <T> T get(String bank_id, String path, Map<String, ?> queryParams, Map<String, String> headers, String token, Class<T> responseType) {
        String baseUrl = banks.getUrlMap().get(bank_id);
        URI uri = UrlBuilder.from(baseUrl)
                .path(path)
                .queryMap(queryParams)
                .build();
        try {
            return webClient.get()
                    .uri(uri)
                    .headers(h -> applyHeaders(h, headers, token))
                    .retrieve()
                    .bodyToMono(responseType)
                    .block(); // блокируем (обычный вызов)
        } catch (WebClientResponseException e) {
            System.err.println("Ошибка запроса: " + e.getStatusCode() + " " + e.getResponseBodyAsString());
            throw e;
        }
    }

    public <B, T> T post(String bank_id, String path, B body, Map<String, String> headers, String token, Class<T> responseType) {
        String baseUrl = banks.getUrlMap().get(bank_id);
        URI uri = UrlBuilder.from(baseUrl)
                .path(path)
                .build();
        try {
            return webClient.post()
                    .uri(uri)
                    .headers(h -> applyHeaders(h, headers, token))
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(responseType)
                    .block();
        } catch (WebClientResponseException e) {
            System.err.println("Ошибка запроса: " + e.getStatusCode() + " " + e.getResponseBodyAsString());
            throw e;
        }
    }

    public void postVoid(String bank_id, String path, Map<String, String> headers, Object body, String token) {
        String baseUrl = banks.getUrlMap().get(bank_id);
        URI uri = UrlBuilder.from(baseUrl)
                .path(path)
                .build();

        try {
            webClient.post()
                    .uri(uri)
                    .headers(h -> applyHeaders(h, headers, token))
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
        } catch (WebClientResponseException e) {
            System.err.println("Ошибка запроса: " + e.getStatusCode() + " " + e.getResponseBodyAsString());
            throw e;
        }
    }

    private void applyHeaders(HttpHeaders httpHeaders, Map<String, String> customHeaders, String token) {
        if (customHeaders != null) {
            customHeaders.forEach((name, value) -> {
                if (HttpHeaders.CONTENT_TYPE.equalsIgnoreCase(name)) {
                    httpHeaders.set(HttpHeaders.CONTENT_TYPE, value);
                } else {
                    httpHeaders.add(name, value);
                }
            });
        }

        if (token == null) {
            return;
        }

        String sanitized = token.strip();
        if (sanitized.isEmpty()) {
            return;
        }

        if (sanitized.regionMatches(true, 0, "Bearer ", 0, 7)) {
            httpHeaders.set(HttpHeaders.AUTHORIZATION, sanitized);
        } else {
            httpHeaders.setBearerAuth(sanitized);
        }
    }
}
