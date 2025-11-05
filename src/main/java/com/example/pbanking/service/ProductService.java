package com.example.pbanking.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.pbanking.dto.response.AvailableProductsResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final WebClientExecutor wc;
    private final BankTokenService tokenService;

    private static final String AVAILABLE_PRODUCTS_PATH = "/products";

    public List<AvailableProductsResponse.Product> getAvailableProducts(String bankId) {
        var resp = wc.get(bankId, AVAILABLE_PRODUCTS_PATH, null, null,
                tokenService.getBankToken(bankId), AvailableProductsResponse.class);
        return resp.products();
    }
}