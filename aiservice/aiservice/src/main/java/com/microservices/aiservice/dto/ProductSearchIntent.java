package com.microservices.aiservice.dto;

import com.microservices.aiservice.enu.Category;

import java.math.BigDecimal;

public record ProductSearchIntent(
        String brand,

        Category category,

        BigDecimal minPrice,

        BigDecimal maxPrice,

        String keyword
) {
}