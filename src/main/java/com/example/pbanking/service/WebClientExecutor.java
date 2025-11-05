package com.example.pbanking.service;

import java.net.URI;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.example.pbanking.config.BanksProperties;
import com.example.pbanking.exception.BadRequestException;
import com.example.pbanking.utils.UrlBuilder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebClientExecutor {
    private final WebClient webClient;
    private final BanksProperties banks;

    public <T> T get(String bank_id, String path, Map<String, ?> queryParams, Map<String, String> headers, String token, Class<T> responseType) {
        String baseUrl = banks.getUrlMap().get(bank_id);
        if (baseUrl == null) {
            throw new BadRequestException("Unknown bank_id: " + bank_id);
        }
        URI uri = UrlBuilder.from(baseUrl)
                .path(path)
                .queryMap(queryParams)
                .build();
        log.debug("GET {}", uri);
        try {
            return webClient.get()
                    .uri(uri)
                    .headers(h -> applyHeaders(h, headers, token))
                    .attribute("appliedToken", sanitizeToken(token))
                    .retrieve()
                    .bodyToMono(responseType)
                    .block();
        } catch (WebClientResponseException e) {
            log.error("GET request failed [{}] with status {} and body {}", uri, e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw e;
        }
    }

    public <B, T> T post(String bank_id, String path, B body, Map<String, ?> queryParams, Map<String, String> headers, String token, Class<T> responseType) {
        String baseUrl = banks.getUrlMap().get(bank_id);
        if (baseUrl == null) {
            throw new BadRequestException("Unknown bank_id: " + bank_id);
        }
        URI uri = UrlBuilder.from(baseUrl)
                .path(path)
                .queryMap(queryParams)
                .build();
        log.debug("POST {}", uri);
        try {
            return webClient.post()
                    .uri(uri)
                    .headers(h -> applyHeaders(h, headers, token))
                    .attribute("appliedToken", sanitizeToken(token))
                    .body(body == null ? BodyInserters.empty() : BodyInserters.fromValue(body))
                    .retrieve()
                    .bodyToMono(responseType)
                    .block();
        } catch (WebClientResponseException e) {
            log.error("POST request failed [{}] with status {} and body {}", uri, e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw e;
        }
    }

    public void postVoid(String bank_id, String path, Object body, Map<String, ?> queryParams, Map<String, String> headers,
            String token) {
        String baseUrl = banks.getUrlMap().get(bank_id);
        if (baseUrl == null) {
            throw new BadRequestException("Unknown bank_id: " + bank_id);
        }
        URI uri = UrlBuilder.from(baseUrl)
                .path(path)
                .queryMap(queryParams)
                .build();
        log.debug("POST {}", uri);

        try {
            webClient.post()
                    .uri(uri)
                    .headers(h -> applyHeaders(h, headers, token))
                    .attribute("appliedToken", sanitizeToken(token))
                    .body(body == null ? BodyInserters.empty() : BodyInserters.fromValue(body))
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
        } catch (WebClientResponseException e) {
            log.error("POST request failed [{}] with status {} and body {}", uri, e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw e;
        }
    }

    private void applyHeaders(HttpHeaders httpHeaders, Map<String, String> customHeaders, String token) {
        log.debug("Initial headers: {}", httpHeaders);
        if (customHeaders != null) {
            customHeaders.forEach((name, value) -> {
                if (HttpHeaders.CONTENT_TYPE.equalsIgnoreCase(name)) {
                    httpHeaders.set(HttpHeaders.CONTENT_TYPE, value);
                } else {
                    httpHeaders.add(name, value);
                }
            });
        }
        String sanitized = sanitizeToken(token);
        if (sanitized != null) {
            if (sanitized.regionMatches(true, 0, "Bearer ", 0, 7)) {
                httpHeaders.set(HttpHeaders.AUTHORIZATION, sanitized);
            } else {
                httpHeaders.setBearerAuth(sanitized);
            }
        }
        log.debug("Authorization header applied: {}", sanitized);
        log.debug("Final headers: {}", httpHeaders);
    }

    private String sanitizeToken(String token) {
        if (token == null) {
            return null;
        }
        String sanitized = token.strip();
        return sanitized.isEmpty() ? null : sanitized;
    }
}
