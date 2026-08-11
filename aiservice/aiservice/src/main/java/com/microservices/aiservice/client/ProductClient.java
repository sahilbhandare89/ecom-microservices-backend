package com.microservices.aiservice.client;

import com.microservices.aiservice.dto.ProductResponse;
import com.microservices.aiservice.enu.Category;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;

@FeignClient(
        name = "product-service"
)
public interface ProductClient {

    @GetMapping("/api/v1/products/all")
    List<ProductResponse> getAllProducts();

    @GetMapping("/api/v1/products/{productId}")
    ProductResponse getProductById(@PathVariable("productId") String productId);

    @GetMapping("/api/v1/products/filter")
    List<ProductResponse> filterProducts(
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String keyword
    );
}