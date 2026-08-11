package com.microservices.aiservice.dto.cart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemResponse {

    private String productId;

    private String productName;

    private Integer quantity;

    private Double price;

    private Double subtotal;

    private String imageUrl;
}