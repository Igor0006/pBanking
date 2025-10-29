package com.example.pbanking.service;

import java.net.URI;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.example.pbanking.utils.UrlBuilder;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class WebClientExecutor {
    private final WebClient webClient;

    public <T> T get(String baseUrl, String path, Map<String, ?> queryParams, String token, Class<T> responseType) {
        URI uri = UrlBuilder.from(baseUrl)
                .path(path)
                .queryMap(queryParams)
                .build();
        try {
            return webClient.get()
                    .uri(uri)
                    .headers(h -> {
                        if (token != null)
                            h.setBearerAuth(token);
                    })
                    .retrieve()
                    .bodyToMono(responseType)
                    .block(); // блокируем (обычный вызов)
        } catch (WebClientResponseException e) {
            System.err.println("Ошибка запроса: " + e.getStatusCode() + " " + e.getResponseBodyAsString());
            throw e;
        }
    }

    public <B, T> T post(String baseUrl, String path, B body, String token, Class<T> responseType) {
        URI uri = UrlBuilder.from(baseUrl)
                .path(path)
                .build();
        try {
            return webClient.post()
                    .uri(uri)
                    .headers(h -> {
                        if (token != null)
                            h.setBearerAuth(token);
                    })
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(responseType)
                    .block();
        } catch (WebClientResponseException e) {
            System.err.println("Ошибка запроса: " + e.getStatusCode() + " " + e.getResponseBodyAsString());
            throw e;
        }
    }

    public void postVoid(String baseUrl, String path, Object body, String token) {
        URI uri = UrlBuilder.from(baseUrl)
                .path(path)
                .build();

        try {
            webClient.post()
                    .uri(uri)
                    .headers(h -> {
                        if (token != null)
                            h.setBearerAuth(token);
                    })
                    .bodyValue(body)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (WebClientResponseException e) {
            System.err.println("Ошибка запроса: " + e.getStatusCode() + " " + e.getResponseBodyAsString());
            throw e;
        }
    }
}
