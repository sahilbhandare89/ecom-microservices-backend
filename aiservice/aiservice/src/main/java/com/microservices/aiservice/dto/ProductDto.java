package com.microservices.aiservice.dto;

import java.math.BigDecimal;
import java.util.List;

public record ProductDto(

        String id,

        String productName,

        String productDescription,

        BigDecimal productPrice,

        String productBrand,

        String productCategory,

        Integer stockQuantity,

        List<String> tags

) {
}