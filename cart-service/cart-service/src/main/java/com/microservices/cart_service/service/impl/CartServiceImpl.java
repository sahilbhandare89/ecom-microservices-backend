package com.microservices.cart_service.service.impl;

import com.microservices.cart_service.client.ProductClient;
import com.microservices.cart_service.dto.AddToCartRequest;
import com.microservices.cart_service.dto.CartItemResponse;
import com.microservices.cart_service.dto.CartResponse;
import com.microservices.cart_service.dto.ProductResponse;
import com.microservices.cart_service.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartServiceImpl implements CartService {

    // Fix 1: Use StringRedisTemplate to prevent Redis serialization/type issues
    private final StringRedisTemplate redisTemplate;

    private final ProductClient productClient;

    private static final String CART_PREFIX = "cart:";
    private static final Duration CART_TTL = Duration.ofDays(30);

    @Override
    public void addToCart(String userId, AddToCartRequest request) {
        log.info("Adding to cart for user [{}] and product [{}]", userId, request.getProductId());

        ProductResponse productResponse = productClient.getProduct(request.getProductId());

        if (productResponse == null) {
            throw new RuntimeException("Product not found with ID: " + request.getProductId());
        }

        String cartKey = CART_PREFIX + userId;

        // Increment quantity cleanly using String representation
        redisTemplate.opsForHash().increment(
                cartKey,
                request.getProductId(),
                request.getQuantity()
        );

        redisTemplate.expire(cartKey, CART_TTL);
        log.info("Successfully added product [{}] to Redis key [{}]", request.getProductId(), cartKey);
    }


    @Override
    public CartResponse getCart(String userId) {

        String cartKey = CART_PREFIX + userId;

        // Read cart entries from Redis
        Map<Object, Object> cartEntries = redisTemplate.opsForHash().entries(cartKey);

        if (cartEntries.isEmpty()) {
            return CartResponse.builder()
                    .userId(userId)
                    .items(Collections.emptyList())
                    .totalAmount(BigDecimal.ZERO)
                    .build();
        }

        List<CartItemResponse> cartItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (Map.Entry<Object, Object> entry : cartEntries.entrySet()) {

            String productId = entry.getKey().toString();
            Integer quantity = Integer.parseInt(entry.getValue().toString());

            ProductResponse product = null;

            // Fetch product details per ID safely
            try {
                product = productClient.getProduct(productId);
            } catch (Exception e) {
                // Prevents Feign exceptions from breaking the entire cart fetch
            }

            // Fallback if product-service is down or product isn't found
            if (product == null) {
                cartItems.add(
                        CartItemResponse.builder()
                                .productId(productId)
                                .productName("Product (" + productId + ")")
                                .price(BigDecimal.ZERO)
                                .quantity(quantity)
                                .subtotal(BigDecimal.ZERO)
                                .build()
                );
                continue;
            }

            BigDecimal price = product.getProductPrice() != null ? product.getProductPrice() : BigDecimal.ZERO;
            BigDecimal subtotal = price.multiply(BigDecimal.valueOf(quantity));
            totalAmount = totalAmount.add(subtotal);

            cartItems.add(
                    CartItemResponse.builder()
                            .productId(product.getId())
                            .productName(product.getProductName())
                            .price(price)
                            .quantity(quantity)
                            .subtotal(subtotal)
                            .imageUrl(product.getImageUrl())
                            .build()
            );
        }

        return CartResponse.builder()
                .userId(userId)
                .items(cartItems)
                .totalAmount(totalAmount)
                .build();
    }

    @Override
    public void updateQuantity(String userId, String productId, Integer quantity) {
        String cartKey = CART_PREFIX + userId;

        Object existing = redisTemplate.opsForHash().get(cartKey, productId);

        if (existing == null) {
            throw new RuntimeException("Product not found in cart");
        }

        if (quantity == null || quantity < 0) {
            throw new RuntimeException("Invalid quantity");
        }

        if (quantity == 0) {
            redisTemplate.opsForHash().delete(cartKey, productId);

            Long size = redisTemplate.opsForHash().size(cartKey);

            if (size != null && size == 0) {
                redisTemplate.delete(cartKey);
            } else {
                redisTemplate.expire(cartKey, CART_TTL);
            }

            return;
        }

        ProductResponse product = productClient.getProduct(productId);

        if (product != null && quantity > product.getStockQuantity()) {
            throw new RuntimeException("Requested quantity exceeds available stock");
        }

        redisTemplate.opsForHash().put(cartKey, productId, String.valueOf(quantity));
        redisTemplate.expire(cartKey, CART_TTL);
    }

    @Override
    public void removeItem(String userId, String productId) {
        String cartKey = CART_PREFIX + userId;

        Object existing = redisTemplate.opsForHash().get(cartKey, productId);

        if (existing == null) {
            throw new RuntimeException("Product not found in cart");
        }

        redisTemplate.opsForHash().delete(cartKey, productId);

        Long size = redisTemplate.opsForHash().size(cartKey);

        if (size != null && size == 0) {
            redisTemplate.delete(cartKey);
        } else {
            redisTemplate.expire(cartKey, CART_TTL);
        }
    }
}