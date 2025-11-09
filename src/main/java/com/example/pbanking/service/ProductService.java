package com.example.pbanking.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.pbanking.config.TPPConfig;
import com.example.pbanking.dto.response.AvailableProductsResponse;
import com.example.pbanking.exception.NotFoundException;
import com.example.pbanking.model.User;
import com.example.pbanking.model.enums.ConsentType;
import com.example.pbanking.repository.CredentialsRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final WebClientExecutor wc;
    private final BankTokenService tokenService;
    private final UserService userService;
    private final ConsentService consentService;
    private final TPPConfig tppConfig;
    private final CredentialsRepository credentialsRepository;

    private static final String AVAILABLE_PRODUCTS_PATH = "/products";
    private static final String PRODUCT_AGREEMENT_PATH = "/product-agreements";

    public List<AvailableProductsResponse.Product> getAvailableProducts(String bankId) {
        var resp = wc.get(bankId, AVAILABLE_PRODUCTS_PATH, null, null,
                tokenService.getBankToken(bankId), AvailableProductsResponse.class);
        return resp.products();
    }

    public Object getAllUserProducts(String bankId) {
        User user = userService.getCurrentUser();
        String clientId = credentialsRepository.findClientIdByUserAndBank(user.getId(), bankId)
                .orElseThrow(() -> new NotFoundException(
                        "No client id for user: " + user.getUsername() + " and bank: " + bankId));

        var queryMap = Map.of("client_id", clientId);
        var headerMap = Map.of("x-product-agreement-consent-id",
                consentService.getConsentForBank(bankId, ConsentType.PRODUCT_READ, null),
                "x-requesting-bank", tppConfig.getRequestingBankId());

        Object response = wc.get(bankId, PRODUCT_AGREEMENT_PATH, queryMap, headerMap, tokenService.getBankToken(bankId),
                Object.class);

        return response;
    }
}
