package com.microservices.aiservice.dto.cart;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartResponse {

    private String cartId;

    private String userId;

    private List<CartItemResponse> items;

    @JsonProperty("totalAmount")
    private Double totalPrice;
}