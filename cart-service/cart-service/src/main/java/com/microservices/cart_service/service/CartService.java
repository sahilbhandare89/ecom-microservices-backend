package com.microservices.cart_service.service;

import com.microservices.cart_service.dto.AddToCartRequest;
import com.microservices.cart_service.dto.CartResponse;

public interface CartService {

    void addToCart(String userId,
                   AddToCartRequest request);

    CartResponse getCart(String userId);

    void updateQuantity(String userId,
                        String productId,
                        Integer quantity);

    void removeItem(String userId,
                    String productId);
//
//    void clearCart(String userId);
}
