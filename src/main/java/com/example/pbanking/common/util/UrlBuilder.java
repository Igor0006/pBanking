package com.example.pbanking.common.util;

import org.springframework.web.util.UriComponentsBuilder;
import java.net.URI;
import java.util.Map;

public class UrlBuilder {

    private final UriComponentsBuilder builder;

    private UrlBuilder(String baseUrl) {
        this.builder = UriComponentsBuilder.fromUriString(baseUrl);
    }

    public static UrlBuilder from(String baseUrl) {
        return new UrlBuilder(baseUrl);
    }

    public UrlBuilder path(String path) {
        builder.path(path);
        return this;
    }

    public UrlBuilder pathSegment(String segment) {
        builder.pathSegment(segment);
        return this;
    }

    public UrlBuilder query(String name, Object value) {
        if (value != null)
            builder.queryParam(name, value);
        return this;
    }

    public UrlBuilder queryMap(Map<String, ?> params) {
        if (params != null) {
            params.forEach(builder::queryParam);
        }
        return this;
    }

    public URI build() {
        return builder.build().toUri();
    }
    
    public String toString() {
        return build().toString();
    }
}