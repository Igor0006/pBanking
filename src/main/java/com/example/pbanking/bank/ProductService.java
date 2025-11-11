package com.example.pbanking.bank;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.pbanking.bank.dto.response.AvailableProductsResponse;
import com.example.pbanking.bank.dto.response.BankProductResponse;
import com.example.pbanking.common.client.WebClientExecutor;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final WebClientExecutor wc;
    private final BankTokenService tokenService;

    private static final String AVAILABLE_PRODUCTS_PATH = "/products";

    public List<BankProductResponse> getAvailableProducts(String bankId) {
        var resp = wc.get(bankId, AVAILABLE_PRODUCTS_PATH, null, null,
                tokenService.getBankToken(bankId), AvailableProductsResponse.class);
        var products = resp == null ? List.<AvailableProductsResponse.Product>of() : resp.products();
        return products.stream()
                .map(product -> BankProductResponse.from(bankId, product))
                .toList();
    }
}
