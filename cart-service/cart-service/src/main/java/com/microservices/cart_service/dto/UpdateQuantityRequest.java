package com.microservices.cart_service.dto;

import jakarta.validation.constraints.Min;

public class UpdateQuantityRequest {
    @Min(1)
    private Integer quantity;
}
