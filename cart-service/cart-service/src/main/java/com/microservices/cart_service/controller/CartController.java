package com.microservices.cart_service.controller;

import com.microservices.cart_service.dto.AddToCartRequest;
import com.microservices.cart_service.dto.CartResponse;
import com.microservices.cart_service.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@CrossOrigin("*")
@RequestMapping("/api/v1")
public class CartController {

    private final CartService cartService;

    @PostMapping("/cart")
    public ResponseEntity<String> addToCart(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody AddToCartRequest request) {

        cartService.addToCart(userId, request);

        return ResponseEntity.ok("Product added to cart");
    }

    @GetMapping("/getcart")
    public ResponseEntity<CartResponse> getCart(
            @RequestHeader("X-User-Id") String userId) {

        return ResponseEntity.ok(cartService.getCart(userId));
    }

    @PutMapping("/cart/{productId}")
    public ResponseEntity<String> updateQuantity(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String productId,
            @RequestParam Integer quantity) {

        cartService.updateQuantity(userId, productId, quantity);

        return ResponseEntity.ok("Quantity updated successfully");
    }

    @DeleteMapping("/cart/{productId}")
    public ResponseEntity<String> removeItem(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String productId) {

        cartService.removeItem(userId, productId);

        return ResponseEntity.ok("Product removed from cart");
    }
}
