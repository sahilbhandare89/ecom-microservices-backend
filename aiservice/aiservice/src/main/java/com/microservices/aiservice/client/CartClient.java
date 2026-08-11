package com.microservices.aiservice.client;

import com.microservices.aiservice.dto.cart.AddToCartRequest;
import com.microservices.aiservice.dto.cart.CartResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "cart-service")
public interface CartClient {

    @PostMapping("/api/v1/cart")
    String addToCart(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody AddToCartRequest request
    );

    @GetMapping("/api/v1/getcart")
    CartResponse getCart(
            @RequestHeader("X-User-Id") String userId
    );

    @DeleteMapping("/api/v1/cart/{productId}")
    String removeItem(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable("productId") String productId
    );
}