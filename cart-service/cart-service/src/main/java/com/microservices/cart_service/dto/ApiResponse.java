package com.microservices.cart_service.dto;

import lombok.*;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse {
    private String message;

    private boolean success;

}
