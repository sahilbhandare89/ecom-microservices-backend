package com.microservices.aiservice.dto.cart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddItemRequest {

    private String userId;

    private String productId;

    private Integer quantity;
}