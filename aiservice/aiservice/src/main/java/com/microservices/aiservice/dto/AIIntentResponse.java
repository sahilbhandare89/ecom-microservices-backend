package com.microservices.aiservice.dto;

import com.microservices.aiservice.enu.Category;
import com.microservices.aiservice.enu.IntentType;

import java.math.BigDecimal;
import java.util.List;

public record AIIntentResponse(

        IntentType intent,

        String brand,

        Category category,

        Integer quantity,

        String productName,

        BigDecimal minPrice,

        BigDecimal maxPrice,

        String keyword,

        List<String> productNames

) {
}