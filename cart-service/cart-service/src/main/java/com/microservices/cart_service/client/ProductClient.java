package com.microservices.cart_service.client;

import com.microservices.cart_service.dto.ProductResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

//@FeignClient(name = "product-service", url = "http://localhost:8081")
@FeignClient(name = "product-service")
public interface ProductClient {

    @GetMapping("/api/v1/products/{id}")
    ProductResponse getProduct(@PathVariable("id") String productId);

    @PostMapping("/api/v1/products/bulk")
    List<ProductResponse> getProducts(@RequestBody List<String> productIds);
}
