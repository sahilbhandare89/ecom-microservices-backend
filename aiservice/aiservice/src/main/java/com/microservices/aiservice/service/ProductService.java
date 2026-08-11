package com.microservices.aiservice.service;

import com.microservices.aiservice.client.ProductClient;
import com.microservices.aiservice.dto.ProductResponse;
import com.microservices.aiservice.enu.Category;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductClient productClient;

    public List<ProductResponse> getAllProducts() {
        return productClient.getAllProducts();
    }

    public List<ProductResponse> filterProducts(
            String brand,
            Category category,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String keyword
    ) {

        return productClient.filterProducts(
                brand,
                category,
                minPrice,
                maxPrice,
                keyword
        );
    }
}